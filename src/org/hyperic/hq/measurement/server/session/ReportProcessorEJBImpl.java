/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.measurement.server.session;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.common.SystemException;
import org.hyperic.hq.measurement.MeasurementConstants;
import org.hyperic.hq.measurement.MeasurementUnscheduleException;
import org.hyperic.hq.measurement.TimingVoodoo;
import org.hyperic.hq.measurement.data.DSNList;
import org.hyperic.hq.measurement.data.MeasurementReport;
import org.hyperic.hq.measurement.data.ValueList;
import org.hyperic.hq.measurement.shared.AvailabilityManagerLocal;
import org.hyperic.hq.measurement.shared.MeasurementManagerLocal;
import org.hyperic.hq.measurement.shared.MeasurementProcessorLocal;
import org.hyperic.hq.measurement.shared.ReportProcessorLocal;
import org.hyperic.hq.measurement.shared.ReportProcessorUtil;
import org.hyperic.hq.measurement.shared.SRNManagerLocal;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.util.StringUtil;

/**
 * @ejb:bean name="ReportProcessor"
 *      jndi-name="ejb/measurement/ReportProcessor"
 *      local-jndi-name="LocalReportProcessor"
 *      view-type="local"
 *      type="Stateless"
 * 
 * @ejb:transaction type="Required"
 */
public class ReportProcessorEJBImpl 
    extends SessionEJB 
    implements SessionBean 
{
    private final Log log = LogFactory.getLog(ReportProcessorEJBImpl.class);

    private final AvailabilityManagerLocal _availMan =
        AvailabilityManagerEJBImpl.getOne();
    private final MeasurementManagerLocal _dmMan =
        MeasurementManagerEJBImpl.getOne();
    private final MeasurementProcessorLocal _measurementProc =
        MeasurementProcessorEJBImpl.getOne();
    
    private void addPoint(List points, Integer metricId, MetricValue[] vals)
    {
        for (int i=0; i<vals.length; i++)
        {
            try {
                //this is just to check if the metricvalue is valid
                //will throw a NumberFormatException if there is a problem
                BigDecimal bigDec = new BigDecimal(vals[i].getValue());
                DataPoint pt = new DataPoint(metricId, vals[i]);
                points.add(pt);
            } catch(NumberFormatException e) {
                log.warn("Unable to insert: " + e.getMessage() +
                         ", metric id=" + metricId);
            }
        }
    }
    
    private void addData(List points, Measurement dm, int dsnId, 
                         MetricValue[] dpts)
    {
        long interval = dm.getInterval();

        // Safeguard against an anomaly
        if (interval <= 0) {
            log.warn("Measurement had bogus interval[" + interval + "]: " + 
                     dm);
            interval = 60 * 1000;
        }

        // Each datapoint corresponds to a set of measurement
        // values for that cycle.
        MetricValue[] passThroughs = new MetricValue[dpts.length];

        for (int i = 0; i < dpts.length; i++)
        {
            // Save data point to DB.
            long retrieval = dpts[i].getTimestamp();
            long adjust = TimingVoodoo.roundDownTime(retrieval, interval);

            // Create new Measurement data point with the adjusted time
            MetricValue modified = new MetricValue(dpts[i].getValue(),
                                                   adjust);
            passThroughs[i] = modified;
        }
        addPoint(points, dm.getId(), passThroughs);
    }

    /**
     * Method which takes data from the agent (or elsewhere) and throws
     * it into the DataManager, doing the right things with all the
     * derived measurements
     *
     * @ejb:interface-method
     */
    public void handleMeasurementReport(MeasurementReport report)
        throws DataInserterException
    {
        DSNList[] dsnLists = report.getClientIdList();

        List dataPoints = new ArrayList(dsnLists.length);
        List availPoints = new ArrayList(dsnLists.length);
        
        for (int i = 0; i < dsnLists.length; i++) {
            Integer dmId = new Integer(dsnLists[i].getClientId());
            Measurement dm = _dmMan.getMeasurement(dmId);
            
            // Can't do much if we can't look up the derived measurement
            // If the measurement is enabled, we just throw away their data
            // instead of trying to throw it into the backfill.  This is 
            // because we don't know the interval to normalize those old
            // points for.  This is still a problem for people who change their
            // collection period, but the instances should be low.
            if (dm == null || !dm.isEnabled())
                continue;
            
            // If this is an availability metric, then tell the cache about it
            boolean availMetric = false;
            MeasurementTemplate tmpl = dm.getTemplate();
            if (tmpl.getAlias().toUpperCase()
                    .equals(MeasurementConstants.CAT_AVAILABILITY) &&
                tmpl.getCategory().getName()
                    .equals(MeasurementConstants.CAT_AVAILABILITY)) {
                availMetric = true;
            }

            ValueList[] valLists = dsnLists[i].getDsns();
            for (int j = 0; j < valLists.length; j++) {
                int dsnId = valLists[j].getDsnId();
                MetricValue[] vals = valLists[j].getValues();

                if (availMetric) {
                    addData(availPoints, dm, dsnId, vals);
                } else {
                    addData(dataPoints, dm, dsnId, vals);
                }
            }
        }

        sendAvailDataToDB(availPoints);
        sendMetricDataToDB(dataPoints);
        // Check the SRNs to make sure the agent is up-to-date
        SRNManagerLocal srnManager = getSRNManager();
        Collection nonEntities = srnManager.reportAgentSRNs(report.getSRNList());
        
        if (report.getAgentToken() != null && nonEntities.size() > 0) {
            // Better tell the agent to stop reporting non-existent entities
            AppdefEntityID[] entIds = (AppdefEntityID[])
                nonEntities.toArray(new AppdefEntityID[nonEntities.size()]);
            try {
                _measurementProc.unschedule(report.getAgentToken(), entIds);
            } catch (MeasurementUnscheduleException e) {
                log.error("Cannot unschedule entities: " +
                          StringUtil.arrayToString(entIds));
            }
        }
    }

    private void sendAvailDataToDB(List availPoints) {
        _availMan.addData(availPoints);
    }

    /**
     * Sends the actual data to the DB.
     */
    private void sendMetricDataToDB(List dataPoints) 
        throws DataInserterException
    {
        DataInserter d = MeasurementStartupListener.getDataInserter();

        try {
            d.insertMetrics(dataPoints);
            int size = dataPoints.size();
            long ts = System.currentTimeMillis();
            ReportStatsCollector.getInstance().getCollector().add(size, ts);
        } catch(InterruptedException e) {
            throw new SystemException("Interrupted while attempting to " + 
                                      "insert data");
        }
    }

    public static ReportProcessorLocal getOne() {
        try {
            return ReportProcessorUtil.getLocalHome().create();
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }
    
    public void ejbCreate(){}
    public void ejbPostCreate(){}
    public void ejbActivate(){}
    public void ejbPassivate(){}
    public void ejbRemove(){}
    public void setSessionContext(SessionContext ctx){}
} 

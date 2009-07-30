/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2008], Hyperic, Inc.
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

package org.hyperic.hq.authz.server.session;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.authz.shared.AuthzConstants;

public class Resource extends AuthzNamedBean implements Comparable {
    public static final Log _log = LogFactory.getLog(Resource.class);

    private ResourceType _resourceType;
    private Resource     _prototype;
    private Integer      _instanceId;
    private AuthzSubject _owner;
    private long         _mtime = System.currentTimeMillis();
    private boolean      _system = false;
    private Collection   _virtuals = new ArrayList();
    private Collection   _fromEdges = new ArrayList();
    private Collection   _toEdges = new ArrayList();
    private Collection   _groupBag = new ArrayList();

    protected Resource() {
    }

    public Resource(ResourceType type, Resource prototype, String name,
             AuthzSubject owner, Integer instanceId, boolean system)
    {
        super(name);
        _resourceType = type;
        _prototype    = prototype;
        _instanceId   = instanceId;
        _owner        = owner;
        _system       = system;
    }

    protected Collection getGroupBag() {
        return _groupBag;
    }

    protected void setGroupBag(Collection b) {
        _groupBag = b;
    }

    public boolean isInAsyncDeleteState() {
        return _resourceType == null;
    }

    public ResourceType getResourceType() {
        return _resourceType;
    }

    protected void setResourceType(ResourceType resourceTypeId) {
        _resourceType = resourceTypeId;
    }

    public Resource getPrototype() {
        return _prototype;
    }

    protected void setPrototype(Resource p) {
        _prototype = p;
    }

    public Integer getInstanceId() {
        return _instanceId;
    }

    protected void setInstanceId(Integer val) {
        _instanceId = val;
    }

    public AuthzSubject getOwner() {
        return _owner;
    }

    protected void setOwner(AuthzSubject val) {
        _owner = val;
    }

    public boolean isSystem() {
        return _system;
    }

    /**
     * Returns true of this is the root resource
     */
    public boolean isRoot() {
        return getId().equals(AuthzConstants.rootResourceId);
    }

    public void markDirty() {
        _mtime = System.currentTimeMillis();
    }

    public void setMtime(long mtime) {
        _mtime = mtime;
    }

    public long getMtime() {
        return _mtime;
    }

    protected void setSystem(boolean fsystem) {
        _system = fsystem;
    }

    public Collection getVirtuals() {
        return _virtuals;
    }

    protected void setVirtuals(Collection virtuals) {
        _virtuals = virtuals;
    }

    protected void setFromEdges(Collection e) {
        _fromEdges = e;
    }

    protected Collection getFromEdges() {
        return _fromEdges;
    }

    protected void setToEdges(Collection e) {
        _toEdges = e;
    }

    protected Collection getToEdges() {
        return _toEdges;
    }

    public boolean isOwner(Integer possibleOwner)
    {
        boolean is = false;

        if (possibleOwner == null) {
            //XXX: Throw exception instead.
            _log.error("possible Owner is NULL. This is probably not " +
                       "what you want.");
        } else {
            // Overlord owns everything.
            if (is = possibleOwner.equals(AuthzConstants.overlordId) ==
                false) {
                if (_log.isDebugEnabled() && possibleOwner != null) {
                    _log.debug("User is " + possibleOwner +
                               " owner is " + getOwner().getId());
                }
                is = (possibleOwner.equals(getOwner().getId()));
            }
        }
        return is;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof Resource) || !super.equals(obj)) {
            return false;
        }
        Resource o = (Resource) obj;
        return
            ((_resourceType == o.getResourceType()) ||
             (_resourceType != null && o.getResourceType() != null &&
              _resourceType.equals(o.getResourceType())))
            &&
            ((_instanceId == o.getInstanceId()) ||
             (_instanceId != null && o.getInstanceId() != null &&
              _instanceId.equals(o.getInstanceId())));
    }

    public int hashCode()
    {
        int result = super.hashCode();

        result = 37 * result + (_resourceType != null ? _resourceType.hashCode() : 0);
        result = 37 * result + (_instanceId != null ? _instanceId.hashCode() : 0);

        return result;
    }

    public int compareTo(Object arg0) {
        if (!(arg0 instanceof Resource) || getSortName() == null ||
                ((Resource) arg0).getSortName() == null)
            return -1;

        return getSortName().compareTo(((Resource) arg0).getSortName());
    }

}

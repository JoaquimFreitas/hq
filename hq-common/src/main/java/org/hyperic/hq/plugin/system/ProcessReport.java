package org.hyperic.hq.plugin.system;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class ProcessReport implements Serializable {


    private static final long serialVersionUID = 1L;
    private long pid;
    private String owner;
    private String size;
    private String resident;
    private String share;
    private String cpuTotal;
    private String cpuPerc;
    private String memPerc;
    private char state;
    private String baseName;
    private String startTime;
    private String[] args;

    public ProcessReport() {
    }

    public ProcessReport(ProcessData process) {
        this.pid = process.getPid();
        this.owner = process.getOwner();
        this.startTime = process.getFormattedStartTime();
        this.size = process.getFormattedSize();
        this.resident = process.getFormattedResident();
        this.share = process.getFormattedShare();
        this.cpuTotal = process.getFormattedCpuTotal();
        this.cpuPerc = process.getFormattedCpuPerc();
        this.memPerc = process.getFormattedMemPerc();
        this.baseName = process.getBaseName();
        this.state = process.getState();

       this.setArgs(process.getProcArgs());
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getResident() {
        return resident;
    }

    public void setResident(String resident) {
        this.resident = resident;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getCpuPerc() {
        return cpuPerc;
    }

    public void setCpuPerc(String cpuPerc) {
        this.cpuPerc = cpuPerc;
    }

    public String getMemPerc() {
        return memPerc;
    }

    public void setMemPerc(String memPerc) {
        this.memPerc = memPerc;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(String cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public char getState() {
        return state;
    }

    public void setState(char state) {
        this.state = state;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        pid = in.readLong();
        owner = in.readUTF();
        size = in.readUTF();
        resident = in.readUTF();
        share = in.readUTF();
        cpuTotal = in.readUTF();
        cpuPerc = in.readUTF();
        memPerc = in.readUTF();
        baseName = in.readUTF();
        startTime = in.readUTF();
        state = in.readChar();
        args = (String[]) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeLong(pid);
        out.writeUTF(owner);
        out.writeUTF(size);
        out.writeUTF(resident);
        out.writeUTF(share);
        out.writeUTF(cpuTotal);
        out.writeUTF(cpuPerc);
        out.writeUTF(memPerc);
        out.writeUTF(baseName);
        out.writeUTF(startTime);
        out.writeChar(state);
        out.writeObject(args);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("pid=").append(pid).append(", owner=").append(owner).append(", size=").append(size).append(", " +
                "" + "resident=").append(resident).append(", share=").append(share).append(", " +
                "" + "cpuTotal=").append(cpuTotal).append(", cpuPerc=").append(cpuPerc).append(", " +
                "" + "memPerc=").append(memPerc).append(", baseName = ").append(baseName).append(", " +
                "" + "startTime = ").append(startTime).append(", args = ");
        for (String arg : args) {
            if (!arg.equalsIgnoreCase("")) {
                sb.append(arg).append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(args);
        result = (prime * result) + ((baseName == null) ? 0 : baseName.hashCode());
        result = (prime * result) + ((cpuPerc == null) ? 0 : cpuPerc.hashCode());
        result = (prime * result) + ((cpuTotal == null) ? 0 : cpuTotal.hashCode());
        result = (prime * result) + ((memPerc == null) ? 0 : memPerc.hashCode());
        result = (prime * result) + ((owner == null) ? 0 : owner.hashCode());
        result = (prime * result) + (int) (pid ^ (pid >>> 32));
        result = (prime * result) + ((resident == null) ? 0 : resident.hashCode());
        result = (prime * result) + ((share == null) ? 0 : share.hashCode());
        result = (prime * result) + ((size == null) ? 0 : size.hashCode());
        result = (prime * result) + ((startTime == null) ? 0 : startTime.hashCode());
        result = (prime * result) + state;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProcessReport other = (ProcessReport) obj;
        if (!Arrays.equals(args, other.args)) {
            return false;
        }
        if (baseName == null) {
            if (other.baseName != null) {
                return false;
            }
        } else if (!baseName.equals(other.baseName)) {
            return false;
        }
        if (cpuPerc == null) {
            if (other.cpuPerc != null) {
                return false;
            }
        } else if (!cpuPerc.equals(other.cpuPerc)) {
            return false;
        }
        if (cpuTotal == null) {
            if (other.cpuTotal != null) {
                return false;
            }
        } else if (!cpuTotal.equals(other.cpuTotal)) {
            return false;
        }
        if (memPerc == null) {
            if (other.memPerc != null) {
                return false;
            }
        } else if (!memPerc.equals(other.memPerc)) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        if (pid != other.pid) {
            return false;
        }
        if (resident == null) {
            if (other.resident != null) {
                return false;
            }
        } else if (!resident.equals(other.resident)) {
            return false;
        }
        if (share == null) {
            if (other.share != null) {
                return false;
            }
        } else if (!share.equals(other.share)) {
            return false;
        }
        if (size == null) {
            if (other.size != null) {
                return false;
            }
        } else if (!size.equals(other.size)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        return true;
    }
}
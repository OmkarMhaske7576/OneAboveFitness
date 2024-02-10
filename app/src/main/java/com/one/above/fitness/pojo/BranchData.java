package com.one.above.fitness.pojo;

public class BranchData {
    private String Branchno;
    private String Branchname;
    private String Branchadd;
    private String status;

    public BranchData() {
    }
    public BranchData(String branchno, String branchname, String branchadd, String status) {
        Branchno = branchno;
        Branchname = branchname;
        Branchadd = branchadd;
        this.status = status;
    }

    public String getBranchno() {
        return Branchno;
    }

    public void setBranchno(String branchno) {
        Branchno = branchno;
    }

    public String getBranchname() {
        return Branchname;
    }

    public void setBranchname(String branchname) {
        Branchname = branchname;
    }

    public String getBranchadd() {
        return Branchadd;
    }

    public void setBranchadd(String branchadd) {
        Branchadd = branchadd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BranchData{" +
                "Branchno='" + Branchno + '\'' +
                ", Branchname='" + Branchname + '\'' +
                ", Branchadd='" + Branchadd + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

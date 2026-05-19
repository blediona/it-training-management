package models.dto;

public class DashboardStats {
    private int totalCandidates;
    private int activeCandidates;
    private int certifiedCandidates;
    private double totalRevenue;
    private double outstandingBalance;

    public DashboardStats(int totalCandidates, int activeCandidates, int certifiedCandidates,
                          double totalRevenue, double outstandingBalance) {
        this.totalCandidates = totalCandidates;
        this.activeCandidates = activeCandidates;
        this.certifiedCandidates = certifiedCandidates;
        this.totalRevenue = totalRevenue;
        this.outstandingBalance = outstandingBalance;
    }

    public int getTotalCandidates() {
        return totalCandidates;
    }
    public int getActiveCandidates() {
        return activeCandidates;
    }
    public int getCertifiedCandidates() {
        return certifiedCandidates;
    }
    public double getTotalRevenue() {
        return totalRevenue;
    }
    public double getOutstandingBalance() {
        return outstandingBalance;
    }
}
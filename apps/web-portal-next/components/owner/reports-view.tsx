"use client";

import { useState, useEffect } from "react";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    FileText,
    TrendingUp,
    DollarSign,
    Leaf,
    CheckCircle2,
    Clock,
    XCircle,
    Download,
    Loader2,
} from "lucide-react";
import {
    getOwnerReportSummary,
    getOwnerMonthlyReport,
    type OwnerReportSummary,
    type OwnerMonthlyReport,
} from "@/lib/api/owner";

export function OwnerReportsView() {
    const [summary, setSummary] = useState<OwnerReportSummary | null>(null);
    const [monthlyData, setMonthlyData] = useState<OwnerMonthlyReport | null>(
        null
    );
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchData();
    }, [selectedYear]);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [summaryData, monthlyReport] = await Promise.all([
                getOwnerReportSummary(),
                getOwnerMonthlyReport(selectedYear),
            ]);
            setSummary(summaryData);
            setMonthlyData(monthlyReport);
        } catch (error) {
            console.error("Failed to load reports:", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
            </div>
        );
    }

    const monthNames = [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
    ];

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">My Reports</h1>
                    <p className="text-muted-foreground">
                        Comprehensive overview of your EV journey performance
                    </p>
                </div>
                <div className="flex gap-2">
                    <Select
                        value={selectedYear.toString()}
                        onValueChange={(val) => setSelectedYear(parseInt(val))}
                    >
                        <SelectTrigger className="w-[120px]">
                            <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="2025">2025</SelectItem>
                            <SelectItem value="2024">2024</SelectItem>
                            <SelectItem value="2023">2023</SelectItem>
                        </SelectContent>
                    </Select>
                </div>
            </div>

            {/* Summary KPI Cards */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                            Total Journeys
                        </CardTitle>
                        <FileText className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{summary?.totalJourneys}</div>
                        <p className="text-xs text-muted-foreground">
                            {summary?.verifiedJourneys} verified
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                            Credits Generated
                        </CardTitle>
                        <Leaf className="h-4 w-4 text-emerald-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {summary?.totalCreditsGenerated.toFixed(2)} tCO2
                        </div>
                        <p className="text-xs text-muted-foreground">
                            {summary?.averageCreditsPerJourney.toFixed(2)} avg per journey
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                            Total Earnings
                        </CardTitle>
                        <DollarSign className="h-4 w-4 text-emerald-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            ${summary?.totalEarnings.toFixed(2)}
                        </div>
                        <p className="text-xs text-muted-foreground">
                            ${summary?.availableBalance.toFixed(2)} available
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">
                            Verification Rate
                        </CardTitle>
                        <TrendingUp className="h-4 w-4 text-emerald-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {summary?.verificationRate.toFixed(1)}%
                        </div>
                        <p className="text-xs text-muted-foreground">Success rate</p>
                    </CardContent>
                </Card>
            </div>

            {/* Journey Status Breakdown */}
            <Card>
                <CardHeader>
                    <CardTitle>Journey Status Breakdown</CardTitle>
                    <CardDescription>
                        Distribution of your submitted journeys
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-3 gap-4">
                        <div className="flex items-center gap-3 p-4 border rounded-lg">
                            <CheckCircle2 className="h-8 w-8 text-emerald-600" />
                            <div>
                                <p className="text-2xl font-bold">
                                    {summary?.verifiedJourneys}
                                </p>
                                <p className="text-sm text-muted-foreground">Verified</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3 p-4 border rounded-lg">
                            <Clock className="h-8 w-8 text-amber-600" />
                            <div>
                                <p className="text-2xl font-bold">{summary?.pendingJourneys}</p>
                                <p className="text-sm text-muted-foreground">Pending</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3 p-4 border rounded-lg">
                            <XCircle className="h-8 w-8 text-red-600" />
                            <div>
                                <p className="text-2xl font-bold">
                                    {summary?.rejectedJourneys}
                                </p>
                                <p className="text-sm text-muted-foreground">Rejected</p>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Monthly Activity Table */}
            <Card>
                <CardHeader>
                    <CardTitle>Monthly Activity Breakdown ({selectedYear})</CardTitle>
                    <CardDescription>
                        Detailed monthly statistics for journeys, credits, and earnings
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                            <tr className="border-b">
                                <th className="text-left p-3 font-medium">Month</th>
                                <th className="text-right p-3 font-medium">Journeys</th>
                                <th className="text-right p-3 font-medium">
                                    Credits (tCO2)
                                </th>
                                <th className="text-right p-3 font-medium">Earnings ($)</th>
                            </tr>
                            </thead>
                            <tbody>
                            {monthNames.map((month, index) => {
                                const monthKey = `${selectedYear}-${String(
                                    index + 1
                                ).padStart(2, "0")}`;
                                const journeys =
                                    monthlyData?.journeysByMonth[monthKey] || 0;
                                const credits =
                                    monthlyData?.creditsByMonth[monthKey] || 0;
                                const earnings =
                                    monthlyData?.earningsByMonth[monthKey] || 0;

                                return (
                                    <tr key={month} className="border-b hover:bg-muted/50">
                                        <td className="p-3 font-medium">{month}</td>
                                        <td className="p-3 text-right">{journeys}</td>
                                        <td className="p-3 text-right">
                                            {typeof credits === "number"
                                                ? credits.toFixed(2)
                                                : "0.00"}
                                        </td>
                                        <td className="p-3 text-right">
                                            ${typeof earnings === "number"
                                            ? earnings.toFixed(2)
                                            : "0.00"}
                                        </td>
                                    </tr>
                                );
                            })}
                            <tr className="font-bold bg-muted">
                                <td className="p-3">Total</td>
                                <td className="p-3 text-right">
                                    {Object.values(monthlyData?.journeysByMonth || {}).reduce(
                                        (a, b) => a + b,
                                        0
                                    )}
                                </td>
                                <td className="p-3 text-right">
                                    {Object.values(monthlyData?.creditsByMonth || {})
                                        .reduce((a, b) => a + b, 0)
                                        .toFixed(2)}
                                </td>
                                <td className="p-3 text-right">
                                    $
                                    {Object.values(monthlyData?.earningsByMonth || {})
                                        .reduce((a, b) => a + b, 0)
                                        .toFixed(2)}
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </CardContent>
            </Card>

            {/* Financial Summary */}
            <Card>
                <CardHeader>
                    <CardTitle>Financial Summary</CardTitle>
                    <CardDescription>
                        Overview of your earnings and withdrawals
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="space-y-4">
                        <div className="flex justify-between items-center p-3 bg-muted rounded-lg">
                            <span className="font-medium">Total Earnings</span>
                            <span className="text-xl font-bold text-emerald-600">
                ${summary?.totalEarnings.toFixed(2)}
              </span>
                        </div>
                        <div className="flex justify-between items-center p-3 bg-muted rounded-lg">
                            <span className="font-medium">Total Withdrawals</span>
                            <span className="text-xl font-bold">
                ${summary?.totalWithdrawals.toFixed(2)}
              </span>
                        </div>
                        <div className="flex justify-between items-center p-3 bg-muted rounded-lg">
                            <span className="font-medium">Pending Withdrawals</span>
                            <span className="text-xl font-bold text-amber-600">
                ${summary?.pendingWithdrawals.toFixed(2)}
              </span>
                        </div>
                        <div className="flex justify-between items-center p-3 bg-emerald-50 dark:bg-emerald-950 rounded-lg border border-emerald-200 dark:border-emerald-800">
                            <span className="font-bold text-lg">Available Balance</span>
                            <span className="text-2xl font-bold text-emerald-600">
                ${summary?.availableBalance.toFixed(2)}
              </span>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FinancialReport } from '@/types';
import { DollarSign, TrendingUp, Clock, Award } from 'lucide-react';

interface FinancialSummaryCardProps {
  report: FinancialReport;
}

export function FinancialSummaryCard({ report }: FinancialSummaryCardProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-gray-600">Total Platform Fees</CardTitle>
            <DollarSign className="w-5 h-5 text-green-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">
            ${report.totalPlatformFees.toFixed(2)}
          </div>
          <p className="text-sm text-gray-500 mt-1">{report.period}</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-gray-600">Total Transactions</CardTitle>
            <TrendingUp className="w-5 h-5 text-blue-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">{report.totalTransactions}</div>
          <p className="text-sm text-gray-500 mt-1">
            Avg: ${report.averageTransactionValue.toFixed(2)}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-gray-600">Pending Payouts</CardTitle>
            <Clock className="w-5 h-5 text-orange-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">
            ${report.pendingPayouts.toFixed(2)}
          </div>
          <p className="text-sm text-gray-500 mt-1">Awaiting approval</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-gray-600">Total Revenue</CardTitle>
            <DollarSign className="w-5 h-5 text-green-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">
            ${report.totalRevenue.toFixed(2)}
          </div>
          <p className="text-sm text-gray-500 mt-1">Gross transaction volume</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-gray-600">Credits Traded</CardTitle>
            <Award className="w-5 h-5 text-green-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">
            {report.totalCreditsTraded.toFixed(1)}
          </div>
          <p className="text-sm text-gray-500 mt-1">Tonnes CO₂</p>
        </CardContent>
      </Card>

      <Card className="bg-gradient-to-br from-green-500 to-green-600 text-white">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium opacity-90">Fee Rate</CardTitle>
            <TrendingUp className="w-5 h-5 opacity-80" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold">
            {((report.totalPlatformFees / report.totalRevenue) * 100).toFixed(1)}%
          </div>
          <p className="text-sm opacity-90 mt-1">Platform commission</p>
        </CardContent>
      </Card>
    </div>
  );
}

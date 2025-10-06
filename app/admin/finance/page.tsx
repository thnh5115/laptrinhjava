'use client';

import { useAppContext } from '@/contexts/AppContext';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { FinancialSummaryCard } from '@/components/admin/FinancialSummaryCard';
import { TransactionsTable } from '@/components/admin/TransactionsTable';
import { PayoutsPanel } from '@/components/admin/PayoutsPanel';
import {
  getFinancialReport,
  getAdminTransactions,
  getAllWithdrawalRequests,
} from '@/lib/mockData';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

export default function AdminFinancePage() {
  const { state } = useAppContext();
  const router = useRouter();

  useEffect(() => {
    if (state.currentUser && state.currentUser.role !== 'admin') {
      router.push('/dashboard');
    }
  }, [state.currentUser, router]);

  if (!state.currentUser || state.currentUser.role !== 'admin') {
    return null;
  }

  const financialReport = getFinancialReport();
  const transactions = getAdminTransactions();
  const withdrawalRequests = getAllWithdrawalRequests();

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Finance & Transactions</h1>
          <p className="text-gray-600">
            Monitor platform transactions, manage payouts, and track financial performance
          </p>
        </div>

        <FinancialSummaryCard report={financialReport} />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <TransactionsTable transactions={transactions} />
          </div>
          <div className="lg:col-span-1">
            <PayoutsPanel withdrawals={withdrawalRequests} />
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

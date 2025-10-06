'use client';

import { useAppContext } from '@/contexts/AppContext';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { WalletBalanceCard } from '@/components/wallet/WalletBalanceCard';
import { TransactionTable } from '@/components/wallet/TransactionTable';
import { WithdrawForm } from '@/components/wallet/WithdrawForm';
import { getWalletByUserId, getWalletTransactionsByUserId } from '@/lib/mockData';

export default function WalletPage() {
  const { state } = useAppContext();

  if (!state.currentUser) return null;

  const wallet = getWalletByUserId(state.currentUser.id);
  const transactions = getWalletTransactionsByUserId(state.currentUser.id);

  if (!wallet) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-full">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Wallet Not Found</h2>
            <p className="text-gray-600">Your wallet is being set up. Please check back shortly.</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Carbon Wallet</h1>
          <p className="text-gray-600">
            Manage your earnings and withdraw funds from carbon credit sales
          </p>
        </div>

        <WalletBalanceCard wallet={wallet} />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <TransactionTable transactions={transactions} />
          </div>
          <div className="lg:col-span-1">
            <WithdrawForm wallet={wallet} />
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

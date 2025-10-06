'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/hooks/use-toast';
import { WithdrawalRequest } from '@/types';
import { CircleCheck as CheckCircle, Circle as XCircle, Clock, User, CreditCard } from 'lucide-react';
import { getUserById } from '@/lib/mockData';

interface PayoutsPanelProps {
  withdrawals: WithdrawalRequest[];
}

export function PayoutsPanel({ withdrawals }: PayoutsPanelProps) {
  const [selectedWithdrawal, setSelectedWithdrawal] = useState<WithdrawalRequest | null>(null);
  const [actionType, setActionType] = useState<'approve' | 'reject' | null>(null);
  const { toast } = useToast();

  const handleAction = (withdrawal: WithdrawalRequest, action: 'approve' | 'reject') => {
    setSelectedWithdrawal(withdrawal);
    setActionType(action);
  };

  const handleConfirmAction = () => {
    if (!selectedWithdrawal || !actionType) return;

    const user = getUserById(selectedWithdrawal.userId);

    if (actionType === 'approve') {
      toast({
        title: 'Withdrawal Approved',
        description: `Payout of $${selectedWithdrawal.amount.toFixed(2)} to ${user?.firstName} ${user?.lastName} has been approved and will be processed.`,
      });
    } else {
      toast({
        title: 'Withdrawal Rejected',
        description: `Payout of $${selectedWithdrawal.amount.toFixed(2)} to ${user?.firstName} ${user?.lastName} has been rejected.`,
        variant: 'destructive',
      });
    }

    setSelectedWithdrawal(null);
    setActionType(null);
  };

  const pendingWithdrawals = withdrawals.filter((w) => w.status === 'pending');

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Pending Payouts</CardTitle>
          <CardDescription>Review and approve withdrawal requests</CardDescription>
        </CardHeader>
        <CardContent>
          {pendingWithdrawals.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <Clock className="w-12 h-12 mx-auto mb-3 text-gray-400" />
              <p className="font-medium">No pending payouts</p>
              <p className="text-sm">All withdrawal requests have been processed</p>
            </div>
          ) : (
            <div className="space-y-4">
              {pendingWithdrawals.map((withdrawal) => {
                const user = getUserById(withdrawal.userId);
                return (
                  <div
                    key={withdrawal.id}
                    className="border rounded-lg p-4 space-y-3 hover:border-gray-400 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="space-y-1">
                        <div className="flex items-center space-x-2">
                          <User className="w-4 h-4 text-gray-500" />
                          <span className="font-semibold text-gray-900">
                            {user?.firstName} {user?.lastName}
                          </span>
                        </div>
                        <div className="flex items-center space-x-2 text-sm text-gray-600">
                          <CreditCard className="w-4 h-4" />
                          <span>Account: {withdrawal.bankDetails?.accountNumber}</span>
                        </div>
                      </div>
                      <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">
                        Pending
                      </Badge>
                    </div>

                    <div className="flex items-center justify-between pt-3 border-t">
                      <div>
                        <div className="text-2xl font-bold text-gray-900">
                          ${withdrawal.amount.toFixed(2)}
                        </div>
                        <div className="text-sm text-gray-500">
                          Requested on{' '}
                          {withdrawal.requestedAt.toLocaleDateString('en-US', {
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                          })}
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleAction(withdrawal, 'reject')}
                          className="text-red-600 border-red-300 hover:bg-red-50"
                        >
                          <XCircle className="w-4 h-4 mr-1" />
                          Reject
                        </Button>
                        <Button
                          size="sm"
                          onClick={() => handleAction(withdrawal, 'approve')}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <CheckCircle className="w-4 h-4 mr-1" />
                          Approve
                        </Button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      <AlertDialog
        open={!!selectedWithdrawal && !!actionType}
        onOpenChange={() => {
          setSelectedWithdrawal(null);
          setActionType(null);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {actionType === 'approve' ? 'Approve Withdrawal' : 'Reject Withdrawal'}
            </AlertDialogTitle>
            <AlertDialogDescription className="space-y-2">
              {selectedWithdrawal && (
                <>
                  <p>
                    You are about to {actionType === 'approve' ? 'approve' : 'reject'} a withdrawal
                    request for:
                  </p>
                  <p className="text-2xl font-bold text-gray-900">
                    ${selectedWithdrawal.amount.toFixed(2)}
                  </p>
                  <p className="mt-4">
                    User: {getUserById(selectedWithdrawal.userId)?.firstName}{' '}
                    {getUserById(selectedWithdrawal.userId)?.lastName}
                  </p>
                  <p>Account: {selectedWithdrawal.bankDetails?.accountNumber}</p>
                  {actionType === 'approve' && (
                    <p className="text-sm text-gray-600 mt-4">
                      Once approved, the funds will be transferred to the user's bank account within
                      3-5 business days.
                    </p>
                  )}
                  {actionType === 'reject' && (
                    <p className="text-sm text-gray-600 mt-4">
                      The user will be notified of the rejection. The funds will remain in their
                      wallet.
                    </p>
                  )}
                </>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmAction}
              className={
                actionType === 'approve'
                  ? 'bg-green-600 hover:bg-green-700'
                  : 'bg-red-600 hover:bg-red-700'
              }
            >
              {actionType === 'approve' ? 'Confirm Approval' : 'Confirm Rejection'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

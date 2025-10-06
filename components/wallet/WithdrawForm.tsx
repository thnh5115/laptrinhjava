'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
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
import { Wallet } from '@/types';

interface WithdrawFormProps {
  wallet: Wallet;
}

export function WithdrawForm({ wallet }: WithdrawFormProps) {
  const [amount, setAmount] = useState('');
  const [accountName, setAccountName] = useState('');
  const [accountNumber, setAccountNumber] = useState('');
  const [routingNumber, setRoutingNumber] = useState('');
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const { toast } = useToast();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const withdrawAmount = parseFloat(amount);

    if (isNaN(withdrawAmount) || withdrawAmount <= 0) {
      toast({
        title: 'Invalid Amount',
        description: 'Please enter a valid withdrawal amount.',
        variant: 'destructive',
      });
      return;
    }

    if (withdrawAmount > wallet.availableBalance) {
      toast({
        title: 'Insufficient Balance',
        description: `You can only withdraw up to $${wallet.availableBalance.toFixed(2)}.`,
        variant: 'destructive',
      });
      return;
    }

    if (!accountName || !accountNumber || !routingNumber) {
      toast({
        title: 'Missing Information',
        description: 'Please fill in all bank account details.',
        variant: 'destructive',
      });
      return;
    }

    setShowConfirmDialog(true);
  };

  const handleConfirmWithdraw = () => {
    toast({
      title: 'Withdrawal Requested',
      description: `Your withdrawal request for $${parseFloat(amount).toFixed(2)} has been submitted and will be processed within 3-5 business days.`,
    });

    setAmount('');
    setAccountName('');
    setAccountNumber('');
    setRoutingNumber('');
    setShowConfirmDialog(false);
  };

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Withdraw Funds</CardTitle>
          <CardDescription>
            Request a withdrawal to your bank account. Funds will be processed within 3-5 business days.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="amount">Withdrawal Amount</Label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">$</span>
                <Input
                  id="amount"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="pl-8"
                />
              </div>
              <p className="text-sm text-gray-500">
                Available balance: ${wallet.availableBalance.toFixed(2)}
              </p>
            </div>

            <div className="space-y-4 pt-4 border-t">
              <h4 className="font-medium text-gray-900">Bank Account Details</h4>

              <div className="space-y-2">
                <Label htmlFor="accountName">Account Holder Name</Label>
                <Input
                  id="accountName"
                  type="text"
                  placeholder="John Doe"
                  value={accountName}
                  onChange={(e) => setAccountName(e.target.value)}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="accountNumber">Account Number</Label>
                  <Input
                    id="accountNumber"
                    type="text"
                    placeholder="••••••••1234"
                    value={accountNumber}
                    onChange={(e) => setAccountNumber(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="routingNumber">Routing Number</Label>
                  <Input
                    id="routingNumber"
                    type="text"
                    placeholder="••••••5678"
                    value={routingNumber}
                    onChange={(e) => setRoutingNumber(e.target.value)}
                  />
                </div>
              </div>
            </div>

            <Button type="submit" className="w-full bg-green-600 hover:bg-green-700">
              Request Withdrawal
            </Button>
          </form>
        </CardContent>
      </Card>

      <AlertDialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Confirm Withdrawal</AlertDialogTitle>
            <AlertDialogDescription className="space-y-2">
              <p>You are about to request a withdrawal of:</p>
              <p className="text-2xl font-bold text-gray-900">${parseFloat(amount || '0').toFixed(2)}</p>
              <p className="mt-4">To account ending in: {accountNumber.slice(-4)}</p>
              <p className="text-sm text-gray-600 mt-4">
                This withdrawal will be processed within 3-5 business days. You will receive a
                confirmation email once the transfer is complete.
              </p>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmWithdraw}
              className="bg-green-600 hover:bg-green-700"
            >
              Confirm Withdrawal
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

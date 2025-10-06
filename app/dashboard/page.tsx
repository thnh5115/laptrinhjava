'use client';

import { useAppContext } from '@/contexts/AppContext';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatsCard } from '@/components/dashboard/StatsCard';
import { DashboardChart } from '@/components/dashboard/DashboardChart';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { 
  Leaf, 
  DollarSign, 
  TrendingUp, 
  Award,
  Users,
  FileCheck
} from 'lucide-react';
import { getDashboardStats } from '@/lib/mockData';

// Mock chart data
const revenueData = [
  { name: 'Jan', value: 400 },
  { name: 'Feb', value: 300 },
  { name: 'Mar', value: 600 },
  { name: 'Apr', value: 800 },
  { name: 'May', value: 700 },
  { name: 'Jun', value: 900 },
];

const emissionsData = [
  { name: 'Jan', value: 2.4 },
  { name: 'Feb', value: 1.8 },
  { name: 'Mar', value: 3.2 },
  { name: 'Apr', value: 4.1 },
  { name: 'May', value: 3.8 },
  { name: 'Jun', value: 4.5 },
];

const transactionData = [
  { name: 'Jan', value: 12, value2: 8 },
  { name: 'Feb', value: 19, value2: 15 },
  { name: 'Mar', value: 24, value2: 18 },
  { name: 'Apr', value: 31, value2: 22 },
  { name: 'May', value: 28, value2: 25 },
  { name: 'Jun', value: 35, value2: 28 },
];

export default function Dashboard() {
  const { state } = useAppContext();
  
  if (!state.currentUser) return null;
  
  const stats = getDashboardStats(state.currentUser.id, state.currentUser.role);
  
  const renderRoleSpecificStats = () => {
    switch (state.currentUser?.role) {
      case 'ev-owner':
        return (
          <>
            <StatsCard
              title="Total Credits Generated"
              value={stats.totalCredits}
              change={stats.monthlyGrowth}
              icon={Leaf}
              trend="up"
            />
            <StatsCard
              title="Total Revenue"
              value={`$${stats.totalRevenue.toFixed(2)}`}
              change={stats.monthlyGrowth}
              icon={DollarSign}
              trend="up"
            />
            <StatsCard
              title="CO₂ Offset"
              value={`${stats.totalCO2Offset.toFixed(1)} tonnes`}
              change={stats.monthlyGrowth}
              icon={Award}
              trend="up"
            />
            <StatsCard
              title="Active Listings"
              value={stats.activeListings}
              change={5.2}
              icon={TrendingUp}
              trend="up"
            />
          </>
        );
      case 'buyer':
        return (
          <>
            <StatsCard
              title="Credits Purchased"
              value={`${stats.totalCredits.toFixed(1)} tonnes`}
              change={stats.monthlyGrowth}
              icon={Award}
              trend="up"
            />
            <StatsCard
              title="Total Investment"
              value={`$${stats.totalRevenue.toFixed(2)}`}
              change={stats.monthlyGrowth}
              icon={DollarSign}
              trend="up"
            />
            <StatsCard
              title="CO₂ Impact"
              value={`${stats.totalCO2Offset.toFixed(1)} tonnes`}
              change={stats.monthlyGrowth}
              icon={Leaf}
              trend="up"
            />
            <StatsCard
              title="Certificates"
              value="3"
              change={12.5}
              icon={FileCheck}
              trend="up"
            />
          </>
        );
      case 'cva-auditor':
        return (
          <>
            <StatsCard
              title="Total Verifications"
              value={stats.totalCredits}
              change={stats.monthlyGrowth}
              icon={FileCheck}
              trend="up"
            />
            <StatsCard
              title="Pending Reviews"
              value={stats.pendingVerifications || 0}
              change={-8.2}
              icon={TrendingUp}
              trend="down"
            />
            <StatsCard
              title="Approval Rate"
              value="94.2%"
              change={2.1}
              icon={Award}
              trend="up"
            />
            <StatsCard
              title="Avg Review Time"
              value="2.3 days"
              change={-15.8}
              icon={TrendingUp}
              trend="up"
            />
          </>
        );
      case 'admin':
        return (
          <>
            <StatsCard
              title="Total Users"
              value={stats.totalUsers || 0}
              change={stats.monthlyGrowth}
              icon={Users}
              trend="up"
            />
            <StatsCard
              title="Total Credits"
              value={`${stats.totalCredits} tonnes`}
              change={stats.monthlyGrowth}
              icon={Leaf}
              trend="up"
            />
            <StatsCard
              title="Platform Revenue"
              value={`$${(stats.totalRevenue * 0.05).toFixed(2)}`}
              change={stats.monthlyGrowth}
              icon={DollarSign}
              trend="up"
            />
            <StatsCard
              title="Active Listings"
              value={stats.activeListings}
              change={8.7}
              icon={TrendingUp}
              trend="up"
            />
          </>
        );
      default:
        return null;
    }
  };

  const renderRoleSpecificCharts = () => {
    switch (state.currentUser?.role) {
      case 'ev-owner':
        return (
          <>
            <DashboardChart
              title="Revenue Over Time"
              description="Monthly revenue from carbon credit sales"
              data={revenueData}
              type="area"
              dataKey="value"
            />
            <DashboardChart
              title="CO₂ Offset Generated"
              description="Monthly CO₂ reduction from EV usage"
              data={emissionsData}
              type="line"
              dataKey="value"
            />
          </>
        );
      case 'buyer':
        return (
          <>
            <DashboardChart
              title="Purchase History"
              description="Monthly carbon credit purchases"
              data={revenueData}
              type="bar"
              dataKey="value"
            />
            <DashboardChart
              title="CO₂ Impact"
              description="Cumulative environmental impact"
              data={emissionsData}
              type="area"
              dataKey="value"
            />
          </>
        );
      case 'cva-auditor':
        return (
          <>
            <DashboardChart
              title="Verification Activity"
              description="Monthly verification requests processed"
              data={transactionData}
              type="bar"
              dataKey="value"
              secondaryDataKey="value2"
              secondaryColor="#F97316"
            />
            <DashboardChart
              title="Review Time Trends"
              description="Average time to complete verifications"
              data={emissionsData}
              type="line"
              dataKey="value"
            />
          </>
        );
      case 'admin':
        return (
          <>
            <DashboardChart
              title="Platform Growth"
              description="User registrations and transaction volume"
              data={transactionData}
              type="area"
              dataKey="value"
              secondaryDataKey="value2"
              secondaryColor="#14B8A6"
            />
            <DashboardChart
              title="Revenue Trends"
              description="Platform fee revenue over time"
              data={revenueData}
              type="line"
              dataKey="value"
            />
          </>
        );
      default:
        return null;
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-600">
            Welcome back, {state.currentUser.firstName}! Here's your overview.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {renderRoleSpecificStats()}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {renderRoleSpecificCharts()}
        </div>

        {/* Recent Activity */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Your latest transactions and updates</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {state.currentUser.role === 'ev-owner' && (
                <>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">Credit verification completed</p>
                      <p className="text-sm text-gray-500">2.5 tonnes CO₂ verified and listed</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">New journey data uploaded</p>
                      <p className="text-sm text-gray-500">67.8 km driven, 12.7 kg CO₂ saved</p>
                    </div>
                  </div>
                </>
              )}
              {state.currentUser.role === 'buyer' && (
                <>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">Purchase completed</p>
                      <p className="text-sm text-gray-500">3.2 tonnes CO₂ credits acquired</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-orange-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">Auction participation</p>
                      <p className="text-sm text-gray-500">Bid placed on 1.8 tonnes listing</p>
                    </div>
                  </div>
                </>
              )}
              {state.currentUser.role === 'cva-auditor' && (
                <>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">Verification approved</p>
                      <p className="text-sm text-gray-500">2.5 tonnes CO₂ credits verified</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">New verification request</p>
                      <p className="text-sm text-gray-500">1.8 tonnes pending review</p>
                    </div>
                  </div>
                </>
              )}
              {state.currentUser.role === 'admin' && (
                <>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">New user registered</p>
                      <p className="text-sm text-gray-500">EV Owner joined the platform</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                    <div>
                      <p className="font-medium">Transaction completed</p>
                      <p className="text-sm text-gray-500">$288 in platform fees generated</p>
                    </div>
                  </div>
                </>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
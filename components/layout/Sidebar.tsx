'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Chrome as Home, Car, ShoppingCart, TrendingUp, FileCheck, Settings, Users, ChartBar as BarChart3, Leaf, Award, Clock, DollarSign, Wallet, Receipt } from 'lucide-react';
import { useAppContext } from '@/contexts/AppContext';
import { cn } from '@/lib/utils';

const roleMenus = {
  'ev-owner': [
    { href: '/dashboard', label: 'Dashboard', icon: Home },
    { href: '/dashboard/journey', label: 'EV Journey', icon: Car },
    { href: '/dashboard/credits', label: 'My Credits', icon: Leaf },
    { href: '/dashboard/listings', label: 'My Listings', icon: TrendingUp },
    { href: '/dashboard/wallet', label: 'Wallet', icon: Wallet },
    { href: '/dashboard/earnings', label: 'Earnings', icon: DollarSign },
    { href: '/dashboard/settings', label: 'Settings', icon: Settings },
  ],
  'buyer': [
    { href: '/dashboard', label: 'Dashboard', icon: Home },
    { href: '/dashboard/marketplace', label: 'Marketplace', icon: ShoppingCart },
    { href: '/dashboard/purchases', label: 'My Purchases', icon: Award },
    { href: '/dashboard/certificates', label: 'Certificates', icon: FileCheck },
    { href: '/dashboard/settings', label: 'Settings', icon: Settings },
  ],
  'cva-auditor': [
    { href: '/dashboard', label: 'Dashboard', icon: Home },
    { href: '/dashboard/verification-queue', label: 'Verification Queue', icon: Clock },
    { href: '/dashboard/audit-history', label: 'Audit History', icon: FileCheck },
    { href: '/dashboard/reports', label: 'Reports', icon: BarChart3 },
    { href: '/dashboard/settings', label: 'Settings', icon: Settings },
  ],
  'admin': [
    { href: '/dashboard', label: 'Dashboard', icon: Home },
    { href: '/dashboard/users', label: 'Users', icon: Users },
    { href: '/admin/finance', label: 'Finance', icon: Receipt },
    { href: '/dashboard/analytics', label: 'Analytics', icon: BarChart3 },
    { href: '/dashboard/marketplace', label: 'Marketplace', icon: ShoppingCart },
    { href: '/dashboard/settings', label: 'Settings', icon: Settings },
  ],
};

export function Sidebar() {
  const { state } = useAppContext();
  const pathname = usePathname();
  
  if (!state.currentUser) return null;
  
  const menuItems = roleMenus[state.currentUser.role] || [];

  return (
    <div className="w-64 bg-white border-r border-gray-200 h-full">
      <div className="p-6">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 bg-green-500 rounded-lg flex items-center justify-center">
            <Leaf className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-semibold text-gray-900">CarbonCredit</h1>
            <p className="text-sm text-gray-500 capitalize">{state.currentUser.role.replace('-', ' ')}</p>
          </div>
        </div>
      </div>
      
      <nav className="px-3 pb-6">
        <ul className="space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.href || 
              (item.href !== '/dashboard' && pathname.startsWith(item.href));
            
            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    'flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-green-50 text-green-700'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <Icon className="w-5 h-5" />
                  <span>{item.label}</span>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>
    </div>
  );
}
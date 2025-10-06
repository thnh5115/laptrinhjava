'use client';

import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { User, CarbonCredit, Listing, Transaction, VerificationRequest, MarketplaceFilters } from '@/types';
import { 
  mockUsers, 
  mockCarbonCredits, 
  mockListings, 
  mockTransactions, 
  mockVerificationRequests 
} from '@/lib/mockData';

interface AppState {
  currentUser: User | null;
  users: User[];
  credits: CarbonCredit[];
  listings: Listing[];
  transactions: Transaction[];
  verificationRequests: VerificationRequest[];
  marketplaceFilters: MarketplaceFilters;
  isLoading: boolean;
}

type AppAction = 
  | { type: 'SET_CURRENT_USER'; payload: User }
  | { type: 'UPDATE_USER'; payload: User }
  | { type: 'ADD_CREDIT'; payload: CarbonCredit }
  | { type: 'UPDATE_CREDIT'; payload: CarbonCredit }
  | { type: 'ADD_LISTING'; payload: Listing }
  | { type: 'UPDATE_LISTING'; payload: Listing }
  | { type: 'ADD_TRANSACTION'; payload: Transaction }
  | { type: 'UPDATE_VERIFICATION'; payload: VerificationRequest }
  | { type: 'SET_MARKETPLACE_FILTERS'; payload: MarketplaceFilters }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'LOGOUT' };

const initialState: AppState = {
  currentUser: null,
  users: mockUsers,
  credits: mockCarbonCredits,
  listings: mockListings,
  transactions: mockTransactions,
  verificationRequests: mockVerificationRequests,
  marketplaceFilters: {},
  isLoading: false
};

function appReducer(state: AppState, action: AppAction): AppState {
  switch (action.type) {
    case 'SET_CURRENT_USER':
      return { ...state, currentUser: action.payload };
    
    case 'UPDATE_USER':
      return {
        ...state,
        users: state.users.map(user => 
          user.id === action.payload.id ? action.payload : user
        ),
        currentUser: state.currentUser?.id === action.payload.id ? action.payload : state.currentUser
      };
    
    case 'ADD_CREDIT':
      return {
        ...state,
        credits: [...state.credits, action.payload]
      };
    
    case 'UPDATE_CREDIT':
      return {
        ...state,
        credits: state.credits.map(credit =>
          credit.id === action.payload.id ? action.payload : credit
        )
      };
    
    case 'ADD_LISTING':
      return {
        ...state,
        listings: [...state.listings, action.payload]
      };
    
    case 'UPDATE_LISTING':
      return {
        ...state,
        listings: state.listings.map(listing =>
          listing.id === action.payload.id ? action.payload : listing
        )
      };
    
    case 'ADD_TRANSACTION':
      return {
        ...state,
        transactions: [...state.transactions, action.payload]
      };
    
    case 'UPDATE_VERIFICATION':
      return {
        ...state,
        verificationRequests: state.verificationRequests.map(req =>
          req.id === action.payload.id ? action.payload : req
        )
      };
    
    case 'SET_MARKETPLACE_FILTERS':
      return {
        ...state,
        marketplaceFilters: action.payload
      };
    
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload
      };
    
    case 'LOGOUT':
      return {
        ...state,
        currentUser: null
      };
    
    default:
      return state;
  }
}

const AppContext = createContext<{
  state: AppState;
  dispatch: React.Dispatch<AppAction>;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
} | null>(null);

export function AppProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(appReducer, initialState);

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        dispatch({ type: 'SET_CURRENT_USER', payload: user });
      } catch (error) {
        console.error('Error loading user from localStorage:', error);
      }
    }
  }, []);

  // Save user to localStorage when currentUser changes
  useEffect(() => {
    if (state.currentUser) {
      localStorage.setItem('currentUser', JSON.stringify(state.currentUser));
    } else {
      localStorage.removeItem('currentUser');
    }
  }, [state.currentUser]);

  const login = async (email: string, password: string): Promise<boolean> => {
    dispatch({ type: 'SET_LOADING', payload: true });
    
    // Simulate API call delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const user = state.users.find(u => u.email === email);
    
    if (user) {
      dispatch({ type: 'SET_CURRENT_USER', payload: user });
      dispatch({ type: 'SET_LOADING', payload: false });
      return true;
    }
    
    dispatch({ type: 'SET_LOADING', payload: false });
    return false;
  };

  const logout = () => {
    dispatch({ type: 'LOGOUT' });
  };

  return (
    <AppContext.Provider value={{ state, dispatch, login, logout }}>
      {children}
    </AppContext.Provider>
  );
}

export const useAppContext = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext must be used within an AppProvider');
  }
  return context;
};
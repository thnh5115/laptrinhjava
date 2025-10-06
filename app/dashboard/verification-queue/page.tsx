'use client';

import { useState } from 'react';
import { useAppContext } from '@/contexts/AppContext';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  CheckCircle, 
  XCircle, 
  Clock, 
  FileText, 
  User,
  MapPin,
  Calendar,
  Car,
  Leaf
} from 'lucide-react';
import { VerificationRequest, CarbonCredit } from '@/types';
import { getUserById } from '@/lib/mockData';

export default function VerificationQueuePage() {
  const { state, dispatch } = useAppContext();
  const [selectedRequest, setSelectedRequest] = useState<string | null>(null);
  const [reviewNotes, setReviewNotes] = useState('');

  // Only show for CVA auditors
  if (state.currentUser?.role !== 'cva-auditor') {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Access Denied</h2>
          <p className="text-gray-600">This section is only available for CVA auditors.</p>
        </div>
      </DashboardLayout>
    );
  }

  const pendingRequests = state.verificationRequests.filter(req => req.status === 'pending');
  const completedRequests = state.verificationRequests.filter(req => req.status !== 'pending');

  const handleApprove = (requestId: string) => {
    const request = state.verificationRequests.find(r => r.id === requestId);
    if (request) {
      const updatedRequest: VerificationRequest = {
        ...request,
        status: 'approved',
        reviewedBy: state.currentUser!.id,
        reviewedAt: new Date(),
        notes: reviewNotes
      };
      
      dispatch({ type: 'UPDATE_VERIFICATION', payload: updatedRequest });
      
      // Update the credit status as well
      const credit = state.credits.find(c => c.id === request.creditId);
      if (credit) {
        const updatedCredit: CarbonCredit = {
          ...credit,
          verificationStatus: 'approved',
          verifiedAt: new Date(),
          cvAuditorId: state.currentUser!.id,
          status: 'available'
        };
        dispatch({ type: 'UPDATE_CREDIT', payload: updatedCredit });
      }
      
      setReviewNotes('');
      setSelectedRequest(null);
    }
  };

  const handleReject = (requestId: string) => {
    const request = state.verificationRequests.find(r => r.id === requestId);
    if (request) {
      const updatedRequest: VerificationRequest = {
        ...request,
        status: 'rejected',
        reviewedBy: state.currentUser!.id,
        reviewedAt: new Date(),
        notes: reviewNotes
      };
      
      dispatch({ type: 'UPDATE_VERIFICATION', payload: updatedRequest });
      
      // Update the credit status as well
      const credit = state.credits.find(c => c.id === request.creditId);
      if (credit) {
        const updatedCredit: CarbonCredit = {
          ...credit,
          verificationStatus: 'rejected',
          status: 'rejected'
        };
        dispatch({ type: 'UPDATE_CREDIT', payload: updatedCredit });
      }
      
      setReviewNotes('');
      setSelectedRequest(null);
    }
  };

  const renderVerificationCard = (request: VerificationRequest) => {
    const credit = state.credits.find(c => c.id === request.creditId);
    const submitter = getUserById(request.submittedBy);
    
    if (!credit || !submitter) return null;

    const statusColor = {
      pending: 'bg-yellow-100 text-yellow-800',
      approved: 'bg-green-100 text-green-800',
      rejected: 'bg-red-100 text-red-800',
      'in-review': 'bg-blue-100 text-blue-800'
    };

    return (
      <Card key={request.id} className="mb-4">
        <CardHeader>
          <div className="flex justify-between items-start">
            <div>
              <CardTitle className="text-lg">
                {credit.amount.toFixed(1)} tonnes CO₂ Credit
              </CardTitle>
              <CardDescription>
                Submitted by {submitter.firstName} {submitter.lastName} • {new Date(request.submittedAt).toLocaleDateString()}
              </CardDescription>
            </div>
            <Badge className={statusColor[request.status]}>
              {request.status.charAt(0).toUpperCase() + request.status.slice(1)}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-3">
                <div className="flex items-center space-x-2">
                  <MapPin className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">{credit.metadata.location}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Calendar className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">{credit.metadata.period}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Car className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">{credit.metadata.vehicleInfo}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Leaf className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">{credit.metadata.co2Calculation.toFixed(1)} tonnes CO₂</span>
                </div>
              </div>
              
              <div>
                <h4 className="font-medium mb-2">Documents</h4>
                <div className="space-y-1">
                  {request.documents.map((doc, index) => (
                    <div key={index} className="flex items-center space-x-2 text-sm">
                      <FileText className="w-4 h-4 text-gray-400" />
                      <span>{doc}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            
            {request.status === 'pending' && (
              <div className="pt-4 border-t">
                {selectedRequest === request.id ? (
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">Review Notes</label>
                      <Textarea
                        value={reviewNotes}
                        onChange={(e) => setReviewNotes(e.target.value)}
                        placeholder="Add your review notes..."
                        rows={3}
                      />
                    </div>
                    <div className="flex space-x-3">
                      <Button
                        onClick={() => handleApprove(request.id)}
                        className="bg-green-600 hover:bg-green-700"
                      >
                        <CheckCircle className="w-4 h-4 mr-2" />
                        Approve
                      </Button>
                      <Button
                        onClick={() => handleReject(request.id)}
                        variant="destructive"
                      >
                        <XCircle className="w-4 h-4 mr-2" />
                        Reject
                      </Button>
                      <Button
                        onClick={() => setSelectedRequest(null)}
                        variant="outline"
                      >
                        Cancel
                      </Button>
                    </div>
                  </div>
                ) : (
                  <Button
                    onClick={() => setSelectedRequest(request.id)}
                    variant="outline"
                  >
                    Review
                  </Button>
                )}
              </div>
            )}
            
            {request.status !== 'pending' && request.notes && (
              <div className="pt-4 border-t">
                <h4 className="font-medium text-sm mb-1">Review Notes</h4>
                <p className="text-sm text-gray-600">{request.notes}</p>
                {request.reviewedBy && request.reviewedAt && (
                  <p className="text-xs text-gray-500 mt-1">
                    Reviewed by {getUserById(request.reviewedBy)?.firstName} {getUserById(request.reviewedBy)?.lastName} on {new Date(request.reviewedAt).toLocaleDateString()}
                  </p>
                )}
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Verification Queue</h1>
          <p className="text-gray-600">Review and verify carbon credit authenticity</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Pending Reviews</p>
                  <p className="text-2xl font-bold">{pendingRequests.length}</p>
                </div>
                <Clock className="w-8 h-8 text-yellow-500" />
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Approved This Month</p>
                  <p className="text-2xl font-bold">{completedRequests.filter(r => r.status === 'approved').length}</p>
                </div>
                <CheckCircle className="w-8 h-8 text-green-500" />
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Approval Rate</p>
                  <p className="text-2xl font-bold">94.2%</p>
                </div>
                <CheckCircle className="w-8 h-8 text-blue-500" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Verification Queue */}
        <Tabs defaultValue="pending" className="w-full">
          <TabsList>
            <TabsTrigger value="pending">
              Pending ({pendingRequests.length})
            </TabsTrigger>
            <TabsTrigger value="completed">
              Completed ({completedRequests.length})
            </TabsTrigger>
          </TabsList>
          
          <TabsContent value="pending" className="mt-6">
            {pendingRequests.length > 0 ? (
              <div>
                {pendingRequests.map(renderVerificationCard)}
              </div>
            ) : (
              <Card>
                <CardContent className="text-center py-12">
                  <Clock className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">No pending verifications</h3>
                  <p className="text-gray-600">All verification requests have been processed.</p>
                </CardContent>
              </Card>
            )}
          </TabsContent>
          
          <TabsContent value="completed" className="mt-6">
            {completedRequests.length > 0 ? (
              <div>
                {completedRequests.map(renderVerificationCard)}
              </div>
            ) : (
              <Card>
                <CardContent className="text-center py-12">
                  <FileText className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">No completed verifications</h3>
                  <p className="text-gray-600">Completed verifications will appear here.</p>
                </CardContent>
              </Card>
            )}
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}
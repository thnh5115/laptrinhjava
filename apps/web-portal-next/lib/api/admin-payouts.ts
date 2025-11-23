import axiosClient from "./axiosClient";

export interface PayoutStatisticsResponse {
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  completedCount: number;
  totalAmount: number;
  pendingAmount: number;
  approvedAmount: number;
  rejectedAmount: number;
  completedAmount: number;
}

export const getPayoutStatistics = async (): Promise<PayoutStatisticsResponse> => {
  const { data } = await axiosClient.get<PayoutStatisticsResponse>("/admin/payouts/statistics");
  return data;
};

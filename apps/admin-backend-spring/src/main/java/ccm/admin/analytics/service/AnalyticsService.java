package ccm.admin.analytics.service;

import ccm.admin.analytics.dto.response.DisputeRatioResponse;
import ccm.admin.analytics.dto.response.SystemKpiResponse;
import ccm.admin.analytics.dto.response.TransactionTrendResponse;

/** service - Service Interface - Calculate KPIs and generate charts */

public interface AnalyticsService {

    
    SystemKpiResponse getSystemKpis();

    
    TransactionTrendResponse getTransactionTrends(int year);

    
    DisputeRatioResponse getDisputeRatios();
}

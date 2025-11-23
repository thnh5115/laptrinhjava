"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Eye,
  CheckCircle2,
  Clock,
  XCircle,
  Loader2,
  AlertCircle,
} from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
// 1. Import cả JourneyResponse từ API để dùng làm kiểu dữ liệu chuẩn
import { getMyJourneys, type JourneyResponse } from "@/lib/api/owner";

export function JourneyHistoryTable() {
  // 2. Sử dụng JourneyResponse cho state (thay vì interface tự chế)
  const [journeys, setJourneys] = useState<JourneyResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const data = await getMyJourneys();
        // API trả về mảng JourneyResponse[] nên gán thẳng được luôn
        setJourneys(data);
      } catch (err) {
        console.error("Failed to load history:", err);
        setError("Failed to load journey history");
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const getStatusIcon = (status: string) => {
    switch (status?.toUpperCase()) {
      case "APPROVED":
      case "VERIFIED":
        return <CheckCircle2 className="h-4 w-4" />;
      case "PENDING":
        return <Clock className="h-4 w-4" />;
      case "REJECTED":
        return <XCircle className="h-4 w-4" />;
      default:
        return null;
    }
  };

  const getStatusVariant = (
    status: string
  ): "default" | "secondary" | "destructive" => {
    switch (status?.toUpperCase()) {
      case "APPROVED":
      case "VERIFIED":
        return "default";
      case "PENDING":
        return "secondary";
      case "REJECTED":
        return "destructive";
      default:
        return "secondary";
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center py-8 text-destructive">
        <AlertCircle className="h-8 w-8 mb-2" />
        <p>{error}</p>
      </div>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>All Journeys</CardTitle>
      </CardHeader>
      <CardContent>
        {journeys.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">
            No journeys submitted yet.
          </p>
        ) : (
          <div className="space-y-4">
            {journeys.map((journey) => (
              <div
                key={journey.id}
                className="flex items-center justify-between border rounded-lg p-4"
              >
                <div className="flex-1 space-y-1">
                  <div className="flex items-center gap-2">
                    <p className="font-medium">
                      {journey.startLocation} → {journey.endLocation}
                    </p>
                    <Badge
                      variant={getStatusVariant(journey.status)}
                      className={
                        journey.status === "APPROVED"
                          ? "bg-emerald-100 text-emerald-900"
                          : journey.status === "PENDING"
                          ? "bg-amber-100 text-amber-900"
                          : ""
                      }
                    >
                      {getStatusIcon(journey.status)}
                      <span className="ml-1 capitalize">
                        {journey.status?.toLowerCase()}
                      </span>
                    </Badge>
                  </div>
                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>{journey.journeyDate}</span>
                    <span>{journey.distanceKm} km</span>
                    <span>{journey.energyUsedKwh} kWh</span>
                    {/* 3. SỬA: Dùng đúng tên trường 'estimatedCredits' */}
                    {journey.estimatedCredits > 0 && (
                      <span className="font-medium text-emerald-600">
                        {journey.estimatedCredits} tCO2
                      </span>
                    )}
                  </div>
                </div>

                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm">
                      <Eye className="h-4 w-4 mr-2" />
                      Details
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Journey Details</DialogTitle>
                      <DialogDescription>
                        Complete information about this journey
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            Date
                          </p>
                          <p className="text-sm">{journey.journeyDate}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            Status
                          </p>
                          <Badge variant={getStatusVariant(journey.status)}>
                            {journey.status}
                          </Badge>
                        </div>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">
                          Route
                        </p>
                        <p className="text-sm">
                          {journey.startLocation} → {journey.endLocation}
                        </p>
                      </div>
                      <div className="grid grid-cols-3 gap-4">
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            Distance
                          </p>
                          <p className="text-sm">{journey.distanceKm} km</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            Energy Used
                          </p>
                          <p className="text-sm">{journey.energyUsedKwh} kWh</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            Est. Credits
                          </p>
                          {/* 4. SỬA: Dùng đúng tên trường 'estimatedCredits' */}
                          <p className="text-sm font-medium text-emerald-600">
                            {journey.estimatedCredits} tCO2
                          </p>
                        </div>
                      </div>

                      {/* Backend chưa trả về verifiedAt trong JourneyResponse mặc định, 
                          nhưng nếu có thì dùng, không thì ẩn đi để tránh lỗi */}
                      {/* {journey.verifiedAt && (
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Verified At</p>
                          <p className="text-sm">{new Date(journey.verifiedAt).toLocaleString()}</p>
                        </div>
                      )} 
                      */}

                      {journey.status === "REJECTED" && (
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">
                            Info
                          </p>
                          <p className="text-sm text-destructive">
                            See CVA Audit Logs for details.
                          </p>
                        </div>
                      )}
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

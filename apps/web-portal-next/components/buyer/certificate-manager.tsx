"use client";

import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FileText, Download, Loader2, ShieldCheck } from "lucide-react";
import { useAuth } from "@/lib/contexts/AuthContext";
// Import API thật
import { getMyCertificates, type Invoice } from "@/lib/api/buyer";

export function CertificateManager() {
  const { user } = useAuth();
  const [certificates, setCertificates] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    const fetchCerts = async () => {
      try {
        setLoading(true);
        // Lấy danh sách hóa đơn/chứng chỉ thật từ backend
        const data = await getMyCertificates(Number(user.id));
        setCertificates(data);
      } catch (error) {
        console.error("Failed to fetch certificates:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchCerts();
  }, [user]);

  // Hàm giả lập download (Vì backend chưa trả về file PDF thật, chỉ trả về đường dẫn chuỗi)
  const handleDownload = (filePath: string) => {
    alert(
      `Downloading certificate from: ${filePath}\n(Tính năng download thật cần tích hợp AWS S3 hoặc File Server)`
    );
  };

  if (loading)
    return (
      <div className="flex justify-center p-12">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    );

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="bg-emerald-50 border-emerald-200">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg text-emerald-700">
              Total Certificates
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-emerald-600" />
              <span className="text-2xl font-bold text-emerald-800">
                {certificates.length}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>My Certificates</CardTitle>
          <CardDescription>
            Official documentation of your carbon offsets
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {certificates.length === 0 ? (
              <div className="text-center py-12 text-muted-foreground">
                No certificates found. Purchases will appear here automatically.
              </div>
            ) : (
              certificates.map((cert) => (
                <div
                  key={cert.id}
                  className="flex items-center justify-between border rounded-lg p-4 hover:bg-slate-50 transition-colors"
                >
                  <div className="flex items-start gap-4">
                    <div className="p-2 bg-emerald-100 rounded-full">
                      <ShieldCheck className="h-6 w-6 text-emerald-600" />
                    </div>
                    <div className="space-y-1">
                      <p className="font-medium">
                        Carbon Offset Certificate #{cert.id}
                      </p>
                      <div className="flex gap-4 text-sm text-muted-foreground">
                        <span>Transaction Ref: #{cert.trId}</span>
                        <span>
                          Issued:{" "}
                          {new Date(cert.issueDate).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>

                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleDownload(cert.filePath)}
                  >
                    <Download className="mr-2 h-4 w-4" />
                    Download PDF
                  </Button>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

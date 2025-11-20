"use client";

import { useEffect, useState } from "react";
import ownerClient from "@/lib/api/ownerClient"; // Import client gọi cổng 8082
import { Wallet, ArrowUpRight, ArrowDownLeft, RefreshCw } from "lucide-react";

interface Transaction {
  id: number;
  type: string;
  amount: number;
  date: string;
  status: string;
}

export function WalletOverview() {
  const [balance, setBalance] = useState(0);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchWalletData = async () => {
    setLoading(true);
    setError("");
    try {
      console.log("Fetching wallet data from Owner Backend (8082)...");

      // Gọi API: GET http://localhost:8082/api/wallets/me
      const res = await ownerClient.get("/wallets/me");

      // Giả sử Backend trả về: { balance: 1000, transactions: [...] }
      setBalance(res.data.balance || 0);
      setTransactions(res.data.transactions || []);
    } catch (err) {
      console.error("Lỗi lấy ví:", err);
      setError("Không thể tải dữ liệu ví. Vui lòng thử lại.");

      // DỮ LIỆU GIẢ (FALLBACK) ĐỂ BẠN TEST GIAO DIỆN KHI BACKEND CHƯA XONG
      setBalance(150.5);
      setTransactions([
        {
          id: 1,
          type: "Bán tín chỉ",
          amount: 50,
          date: "2023-11-20",
          status: "Hoàn thành",
        },
        {
          id: 2,
          type: "Thưởng hành trình",
          amount: 10,
          date: "2023-11-19",
          status: "Hoàn thành",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWalletData();
  }, []);

  return (
    <div className="space-y-6">
      {/* 1. Card Số Dư */}
      <div className="p-6 bg-gradient-to-r from-green-600 to-emerald-600 rounded-2xl text-white shadow-lg">
        <div className="flex justify-between items-center">
          <div>
            <p className="text-green-100 text-sm font-medium mb-1">
              Tổng số dư khả dụng
            </p>
            <h2 className="text-4xl font-bold flex items-baseline gap-2">
              {loading ? "..." : balance}
              <span className="text-lg font-normal opacity-80">Credits</span>
            </h2>
          </div>
          <div className="p-3 bg-white/20 rounded-full backdrop-blur-sm">
            <Wallet className="w-8 h-8 text-white" />
          </div>
        </div>

        <div className="mt-6 flex gap-3">
          <button className="flex items-center gap-2 px-4 py-2 bg-white text-green-700 rounded-lg font-semibold hover:bg-green-50 transition">
            <ArrowUpRight className="w-4 h-4" /> Rút Tiền
          </button>
          <button
            onClick={fetchWalletData}
            className="flex items-center gap-2 px-4 py-2 bg-green-700/50 text-white rounded-lg hover:bg-green-700 transition"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />{" "}
            Làm mới
          </button>
        </div>
      </div>

      {/* 2. Lịch sử giao dịch */}
      <div className="bg-white rounded-xl border shadow-sm">
        <div className="p-4 border-b bg-gray-50 rounded-t-xl">
          <h3 className="font-semibold text-gray-800">Giao dịch gần đây</h3>
        </div>

        <div className="p-0">
          {error && <p className="p-4 text-red-500 text-sm">{error}</p>}

          <table className="w-full text-sm text-left">
            <thead className="text-gray-500 bg-gray-50 border-b">
              <tr>
                <th className="px-6 py-3">Loại giao dịch</th>
                <th className="px-6 py-3">Ngày</th>
                <th className="px-6 py-3">Trạng thái</th>
                <th className="px-6 py-3 text-right">Số lượng</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <tr
                  key={tx.id}
                  className="border-b hover:bg-gray-50 last:border-0"
                >
                  <td className="px-6 py-4 font-medium text-gray-900">
                    {tx.type}
                  </td>
                  <td className="px-6 py-4 text-gray-500">{tx.date}</td>
                  <td className="px-6 py-4">
                    <span className="px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs">
                      {tx.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-green-600">
                    +{tx.amount}
                  </td>
                </tr>
              ))}
              {!loading && transactions.length === 0 && (
                <tr>
                  <td
                    colSpan={4}
                    className="px-6 py-8 text-center text-gray-500"
                  >
                    Chưa có giao dịch nào.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

import type {Metadata} from "next";

export const metadata: Metadata = {
    title: "My Reports | EV Owner Portal",
    description: "View your EV journey reports and statistics",
};

export default function ReportsLayout({
                                          children,
                                      }: {
    children: React.ReactNode;
}) {
    return <>{children}</>;
}
import { Skeleton } from "@/components/ui/skeleton"

export default function Loading() {
  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Skeleton className="h-9 w-48" />
        <Skeleton className="h-4 w-64" />
      </div>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[0, 1, 2, 3].map((key) => (
          <div key={key} className="space-y-3 rounded-lg border p-4">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-6 w-16" />
            <Skeleton className="h-3 w-28" />
          </div>
        ))}
      </div>
      <Skeleton className="h-[320px] w-full" />
      <div className="grid gap-4 md:grid-cols-2">
        <Skeleton className="h-[260px] w-full" />
        <Skeleton className="h-[260px] w-full" />
      </div>
    </div>
  )
}

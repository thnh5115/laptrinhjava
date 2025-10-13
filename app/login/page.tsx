import { LoginForm } from "@/components/auth/login-form"
import { Leaf } from "lucide-react"

export default function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-neutral-950 dark:via-neutral-900 dark:to-emerald-950">
      <div className="w-full max-w-md p-8">
        <div className="flex flex-col items-center mb-8">
          <div className="flex items-center gap-2 mb-2">
            <div className="p-2 bg-emerald-600 rounded-lg">
              <Leaf className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-2xl font-bold text-foreground">CarbonCredit</h1>
          </div>
          <p className="text-muted-foreground text-center">Trade carbon credits from your EV journeys</p>
        </div>
        <LoginForm />
      </div>
    </div>
  )
}

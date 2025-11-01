import { RegisterForm } from "@/components/auth/register-form"
import { Leaf } from "lucide-react"
import Link from "next/link"

export default function RegisterPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 via-white to-teal-50 dark:from-neutral-950 dark:via-neutral-900 dark:to-emerald-950">
      <div className="w-full max-w-2xl p-8">
        <div className="flex flex-col items-center mb-8">
          <div className="flex items-center gap-2 mb-2">
            <div className="p-2 bg-emerald-600 rounded-lg">
              <Leaf className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-2xl font-bold text-foreground">CarbonCredit</h1>
          </div>
          <p className="text-muted-foreground text-center">Create your account and start trading carbon credits</p>
        </div>
        <RegisterForm />
        <p className="text-center text-sm text-muted-foreground mt-6">
          Already have an account?{" "}
          <Link href="/login" className="text-emerald-600 hover:text-emerald-700 font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}

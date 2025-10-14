"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { mockUsers } from "@/lib/mock-data"

export function LoginForm() {
  const router = useRouter()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsLoading(true)

    // Simulate API delay
    await new Promise((resolve) => setTimeout(resolve, 500))

    // Mock authentication
    const user = mockUsers.find((u) => u.email === email && u.password === password)

    if (user) {
      // Store user session in localStorage
      localStorage.setItem("currentUser", JSON.stringify(user))

      // Redirect based on role
      switch (user.role) {
        case "ev-owner":
          router.push("/ev-owner/dashboard")
          break
        case "buyer":
          router.push("/buyer/dashboard")
          break
        case "cva":
          router.push("/cva/dashboard")
          break
        case "admin":
          router.push("/admin/dashboard")
          break
      }
    } else {
      setError("Invalid email or password")
      setIsLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Sign In</CardTitle>
        <CardDescription>Enter your credentials to access your account</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="user@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700" disabled={isLoading}>
            {isLoading ? "Signing in..." : "Sign In"}
          </Button>

          <p className="text-center text-sm text-muted-foreground">
            Don't have an account?{" "}
            <a href="/auth/register" className="text-emerald-600 hover:text-emerald-700 font-medium">
              Sign up
            </a>
          </p>

          <div className="mt-6 p-4 bg-muted rounded-lg">
            <p className="text-sm font-medium mb-2">Demo Accounts:</p>
            <div className="space-y-1 text-xs text-muted-foreground">
              <p>EV Owner: owner@example.com / password</p>
              <p>Buyer: buyer@example.com / password</p>
              <p>CVA: cva@example.com / password</p>
              <p>Admin: admin@example.com / password</p>
            </div>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

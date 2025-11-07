"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useAuth } from "@/lib/contexts/AuthContext"

export function LoginForm() {
  const { login, status, error: authError, clearError } = useAuth()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [localError, setLocalError] = useState("")
  const [mounted, setMounted] = useState(false)
  const [hasInteracted, setHasInteracted] = useState(false)

  // Prevent hydration mismatch by only rendering loading state after mount
  useEffect(() => {
    setMounted(true)
  }, [])

  // Clear errors when component unmounts or when user starts typing
  useEffect(() => {
    return () => clearError();
  }, [clearError]);

  useEffect(() => {
    // Clear errors when user types
    if (email || password) {
      setLocalError("");
      clearError();
    }
  }, [email, password, clearError]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Prevent auto-submit from browser autofill
    if (!hasInteracted) {
      console.warn('[LoginForm] Blocked auto-submit - no user interaction detected')
      return
    }
    
    setLocalError("")
    clearError()

    // Basic validation
    if (!email || !password) {
      setLocalError("Please enter both email and password")
      return
    }

    if (!email.includes("@")) {
      setLocalError("Please enter a valid email address")
      return
    }

    try {
      // login() will automatically redirect to appropriate dashboard after success
      await login(email, password)
      // No need to manually redirect - AuthContext handles it
    } catch (err: any) {
      // Error is already set in AuthContext
      // Just log it here for debugging
      console.error('[LoginForm] Login failed:', err)
    }
  }

  // Display error from AuthContext or local validation error
  const displayError = authError || localError

  // Prevent hydration mismatch - only show loading state after client mount
  const isLoading = mounted && status === 'loading'

  return (
    <Card>
      <CardHeader>
        <CardTitle>Sign In</CardTitle>
        <CardDescription>Enter your credentials to access your account</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} autoComplete="off" className="space-y-4">
          {displayError && (
            <Alert variant="destructive">
              <AlertDescription>{displayError}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="user@example.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value)
                setHasInteracted(true)
              }}
              onFocus={() => setHasInteracted(true)}
              disabled={isLoading}
              required
              autoComplete="off"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value)
                setHasInteracted(true)
              }}
              onFocus={() => setHasInteracted(true)}
              disabled={isLoading}
              required
              autoComplete="off"
            />
          </div>

          <Button 
            type="submit" 
            className="w-full bg-emerald-600 hover:bg-emerald-700" 
            disabled={isLoading}
            onClick={() => setHasInteracted(true)}
          >
            {isLoading ? (
              <>
                <div className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                Signing in...
              </>
            ) : (
              "Sign In"
            )}
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
              <p className="font-semibold text-emerald-600">Admin: admin@gmail.com / password</p>
            </div>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}


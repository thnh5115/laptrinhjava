"use client"

import type React from "react"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Upload, CheckCircle2 } from "lucide-react"

export function JourneyUploadForm() {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [formData, setFormData] = useState({
    date: "",
    startLocation: "",
    endLocation: "",
    distance: "",
    energyUsed: "",
    vehicleModel: "",
    notes: "",
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1500))

    setIsSubmitting(false)
    setIsSuccess(true)

    // Reset form after 3 seconds
    setTimeout(() => {
      setIsSuccess(false)
      setFormData({
        date: "",
        startLocation: "",
        endLocation: "",
        distance: "",
        energyUsed: "",
        vehicleModel: "",
        notes: "",
      })
    }, 3000)
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }))
  }

  const estimatedCredits =
    formData.distance && formData.energyUsed
      ? ((Number.parseFloat(formData.distance) * 0.32) / 1000).toFixed(2)
      : "0.00"

  return (
    <Card>
      <CardHeader>
        <CardTitle>Journey Details</CardTitle>
        <CardDescription>Provide information about your EV journey to calculate carbon credits</CardDescription>
      </CardHeader>
      <CardContent>
        {isSuccess && (
          <Alert className="mb-6 border-emerald-600 bg-emerald-50 dark:bg-emerald-950">
            <CheckCircle2 className="h-4 w-4 text-emerald-600" />
            <AlertDescription className="text-emerald-900 dark:text-emerald-100">
              Journey submitted successfully! It will be reviewed by a CVA shortly.
            </AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="date">Journey Date</Label>
              <Input
                id="date"
                name="date"
                type="date"
                value={formData.date}
                onChange={handleChange}
                required
                max={new Date().toISOString().split("T")[0]}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="vehicleModel">Vehicle Model</Label>
              <Input
                id="vehicleModel"
                name="vehicleModel"
                placeholder="e.g., Tesla Model 3"
                value={formData.vehicleModel}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="startLocation">Start Location</Label>
            <Input
              id="startLocation"
              name="startLocation"
              placeholder="e.g., San Francisco, CA"
              value={formData.startLocation}
              onChange={handleChange}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="endLocation">End Location</Label>
            <Input
              id="endLocation"
              name="endLocation"
              placeholder="e.g., San Jose, CA"
              value={formData.endLocation}
              onChange={handleChange}
              required
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="distance">Distance (miles)</Label>
              <Input
                id="distance"
                name="distance"
                type="number"
                step="0.1"
                placeholder="48.5"
                value={formData.distance}
                onChange={handleChange}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="energyUsed">Energy Used (kWh)</Label>
              <Input
                id="energyUsed"
                name="energyUsed"
                type="number"
                step="0.1"
                placeholder="12.5"
                value={formData.energyUsed}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="notes">Additional Notes (Optional)</Label>
            <Textarea
              id="notes"
              name="notes"
              placeholder="Any additional information about this journey..."
              value={formData.notes}
              onChange={handleChange}
              rows={3}
            />
          </div>

          {formData.distance && formData.energyUsed && (
            <Alert>
              <AlertDescription>
                <span className="font-medium">Estimated Carbon Credits:</span> {estimatedCredits} tCO₂
              </AlertDescription>
            </Alert>
          )}

          <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700" disabled={isSubmitting}>
            {isSubmitting ? (
              "Submitting..."
            ) : (
              <>
                <Upload className="mr-2 h-4 w-4" />
                Submit Journey
              </>
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

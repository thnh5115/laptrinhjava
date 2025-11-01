"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { useToast } from "@ui/hooks/use-toast"

interface DisputeResolutionFormProps {
  disputeId: string
  onSubmit: () => void
}

export function DisputeResolutionForm({ disputeId, onSubmit }: DisputeResolutionFormProps) {
  const [action, setAction] = useState<string>("approve")
  const [notes, setNotes] = useState("")
  const { toast } = useToast()

  const handleSubmit = () => {
    toast({
      title: "Resolution Submitted",
      description: "The dispute has been resolved successfully.",
    })
    onSubmit()
  }

  return (
    <div className="space-y-4">
      <div>
        <Label>Resolution Action</Label>
        <RadioGroup value={action} onValueChange={setAction} className="mt-2">
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="approve" id="approve" />
            <Label htmlFor="approve">Approve Transaction</Label>
          </div>
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="refund" id="refund" />
            <Label htmlFor="refund">Issue Refund</Label>
          </div>
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="reject" id="reject" />
            <Label htmlFor="reject">Reject Claim</Label>
          </div>
        </RadioGroup>
      </div>

      <div>
        <Label>Resolution Notes</Label>
        <Textarea
          placeholder="Provide details about your decision..."
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          className="mt-1"
          rows={4}
        />
      </div>

      <Button onClick={handleSubmit} className="w-full">
        Submit Resolution
      </Button>
    </div>
  )
}

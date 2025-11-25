import { z } from "zod";

export const userRoleOptions = ["ev-owner", "buyer", "cva", "admin"] as const;

export const registerSchema = z
  .object({
    fullName: z.string().min(2, "Please enter your name"),
    email: z.string().email("Enter a valid email"),
    password: z.string().min(6, "Password must be at least 6 characters"),
    confirmPassword: z.string().min(6, "Confirm your password"),
    role: z.enum(userRoleOptions),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ["confirmPassword"],
    message: "Passwords do not match",
  });

export type RegisterFormData = z.infer<typeof registerSchema>;

export const adminCreateUserSchema = z.object({
  fullName: z.string().min(2, "Please enter full name"),
  email: z.string().email("Enter a valid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
  role: z.enum(userRoleOptions),
  active: z.boolean().optional().default(true),
});

export type AdminCreateUserInput = z.infer<typeof adminCreateUserSchema>;

export const uiRoleToApiRole = (role: (typeof userRoleOptions)[number]) => {
  const map: Record<(typeof userRoleOptions)[number], string> = {
    "ev-owner": "EV_OWNER",
    buyer: "BUYER",
    cva: "CVA",
    admin: "ADMIN",
  };
  return map[role] ?? role;
};

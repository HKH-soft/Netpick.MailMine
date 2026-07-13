import ResetPasswordForm from "@/components/auth/ResetPasswordForm";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Reset Password | Netpick",
};

export default function ResetPasswordPage() {
    return <ResetPasswordForm />;
}




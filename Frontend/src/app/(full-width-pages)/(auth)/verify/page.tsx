import VerifyForm from "@/components/auth/VerifyForm";
import { Metadata } from "next";

export const metadata: Metadata = {
    title: "Verify Email | MailMine",
    description: "Verify your email address to complete registration",
};

export default function VerifyPage() {
    return <VerifyForm />;
}

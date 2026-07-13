import VerifyForm from "@/components/auth/VerifyForm";
import { Metadata } from "next";

export const metadata: Metadata = {
    title: "Verify Email | Netpick",
    description: "Verify your email address to complete registration",
};

export default function VerifyPage() {
    return <VerifyForm />;
}




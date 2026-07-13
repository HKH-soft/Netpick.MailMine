import SignUpForm from "@/components/auth/SignUpForm";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Sign Up | Netpick",
  description: "Create your Netpick account",
};

export default function SignUp() {
  return <SignUpForm />;
}




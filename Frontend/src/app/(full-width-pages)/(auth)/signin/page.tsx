import SignInForm from "@/components/auth/SignInForm";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Sign In | Netpick",
  description: "Sign in to your Netpick account",
};

export default function SignIn() {
  return <SignInForm />;
}




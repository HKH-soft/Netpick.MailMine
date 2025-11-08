// ModalForm.tsx
"use client";
import React from "react";
import { Modal } from "../ui/modal";
import Button from "../ui/button/Button";
import Label from "../form/Label";

import Checkbox from "../form/input/Checkbox";
import { Formik, Form, Field, FieldProps, ErrorMessage } from "formik";
import * as Yup from "yup";

interface FieldConfig {
  name: string;
  label: string;
  type: string;
  required?: boolean;
  placeholder?: string;
}

interface ModalFormProps {
  isOpen: boolean;
  onCloseAction: () => void;
  onSubmit: (data: Record<string, unknown>) => void;
  title: string;
  fields: FieldConfig[];
  initialData?: Record<string, unknown>;
  submitButtonText?: string;
}

const ModalForm: React.FC<ModalFormProps> = ({
  isOpen,
  onCloseAction,
  onSubmit,
  title,
  fields,
  initialData,
  submitButtonText = "Save"
}) => {
  // Create validation schema based on fields
  const validationSchema = Yup.object().shape(
    fields.reduce((acc, field) => {
      if (field.required) {
        switch (field.type) {
          case "email":
            acc[field.name] = Yup.string().email("Invalid email").required("Required");
            break;
          case "password":
            acc[field.name] = Yup.string().min(6, "Password must be at least 6 characters").required("Required");
            break;
          default:
            acc[field.name] = Yup.string().required("Required");
        }
      }
      return acc;
    }, {} as Record<string, Yup.AnySchema>)
  );

  // Create initial values
  const initialValues = fields.reduce((acc, field) => {
    acc[field.name] = (initialData?.[field.name] as string | boolean) ?? (field.type === "checkbox" ? false : "");
    return acc;
  }, {} as Record<string, string | boolean>);

  const handleSubmit = (values: Record<string, string | boolean>, { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }) => {
    onSubmit(values as unknown as Record<string, unknown>);
    setSubmitting(false);
    onCloseAction();
  };

  return (
    <Modal isOpen={isOpen} onCloseAction={onCloseAction} className="max-w-[500px] p-6">
      <div className="rounded-lg">
        <h3 className="mb-4 text-xl font-semibold text-gray-800 dark:text-white/90">
          {title}
        </h3>
        <Formik
          initialValues={initialValues}
          validationSchema={validationSchema}
          onSubmit={handleSubmit}
          enableReinitialize
        >
          {({ isSubmitting, isValid, dirty }) => (
            <Form>
              <div className="space-y-4">
                {fields.map(field => (
                  <div key={field.name}>
                    <Label htmlFor={field.name}>
                      {field.label} {field.required && <span className="text-red-500">*</span>}
                    </Label>
                    {field.type === "textarea" ? (
                      <>
                        <Field
                          as="textarea"
                          id={field.name}
                          name={field.name}
                          placeholder={field.placeholder}
                          className="w-full rounded-lg border px-4 py-2.5 text-sm shadow-theme-xs focus:outline-hidden bg-transparent text-gray-800 border-gray-300 focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                        />
                        <ErrorMessage name={field.name} component="div" className="text-red-500 text-sm mt-1" />
                      </>
                    ) : field.type === "checkbox" ? (
                      <Field name={field.name}>
                        {({ field: formikField }: FieldProps) => (
                          <Checkbox
                            id={field.name}
                            label={field.label}
                            checked={formikField.value as boolean || false}
                            onChange={(checked) => formikField.onChange({ target: { name: field.name, value: checked } })}
                          />
                        )}
                      </Field>
                    ) : (
                      <>
                        <Field
                          type={field.type}
                          id={field.name}
                          name={field.name}
                          placeholder={field.placeholder}
                          className="h-11 w-full rounded-lg border appearance-none px-4 py-2.5 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800 bg-transparent text-gray-800 border-gray-300 focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                        />
                        <ErrorMessage name={field.name} component="div" className="text-red-500 text-sm mt-1" />
                      </>
                    )}
                  </div>
                ))}
              </div>
              <div className="flex justify-end gap-3 mt-6">
                <Button
                  variant="outline"
                  onClick={onCloseAction}
                  className="px-4 py-2"
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  className="px-4 py-2"
                  disabled={!isValid || !dirty || isSubmitting}
                >
                  {isSubmitting ? "Submitting..." : submitButtonText}
                </Button>
              </div>
            </Form>
          )}
        </Formik>
      </div>
    </Modal>
  );
};

export default ModalForm;
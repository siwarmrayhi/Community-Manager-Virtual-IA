import { SubscriptionStatus } from './enums';

export interface ServiceDescriptionResponse {
  name: string;
  description: string;
  monthlyPrice: number;
  currency: string;
  includedNetworks: string[];
  validationFlow: string[];
  contentTypes: string[];
}

export interface SubscriptionResponse {
  id: number;
  userEmail: string;
  status: SubscriptionStatus;
  monthlyPrice: number;
  currency: string;
  paypalSubscriptionId: string | null;
  paypalApprovalUrl: string | null;
  startedAt: string | null;   // instant ISO
  cancelledAt: string | null;
}

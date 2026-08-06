import { Network, Tone, Language, Frequency } from './enums';

export interface ConfigurationRequest {
  userEmail: string;
  targetNetworks: Network[];
  tone?: Tone;
  language?: Language;
  frequency?: Frequency;
  sector: string;
  keywords?: string[];
  profileDescription?: string;
}

export interface ConfigurationResponse {
  id: number;
  subscriptionId: number;
  targetNetworks: Network[];
  tone: Tone;
  language: Language;
  frequency: Frequency;
  sector: string;
  keywords: string[];
  profileDescription: string;
  createdAt: string;
  updatedAt: string;
}

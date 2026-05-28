export type CommunicationType = 'EMAIL' | 'PHONE_CALL' | 'MEETING';

export interface CustomerCommunication {
  id: number;
  customerId: number;
  type: CommunicationType;
  communicationDate: string;
  summary: string;
  createdAt?: string;
}

export interface CustomerCommunicationRequest {
  type: CommunicationType;
  communicationDate: string;
  summary: string;
}
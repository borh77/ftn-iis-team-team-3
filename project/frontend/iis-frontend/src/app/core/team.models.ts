import { UserRole } from './auth/auth.models';

export interface TeamMember {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  role: UserRole;
}

export interface PricelistTeam {
  id: number;
  name: string;
  leaderId: number;
  leaderUsername: string | null;
  memberIds: number[];
  members: TeamMember[];
}

export interface CreateTeamPayload {
  teamName: string;
}

export interface TeamMemberRequestPayload {
  memberId: number;
}
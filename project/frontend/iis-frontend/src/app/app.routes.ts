import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { passwordChangeGuard } from './core/auth/password-change.guard';
import { LoginComponent } from './pages/login/login.component';
import { AdminUsersPageComponent } from './pages/admin-users/admin-users-page.component';
import { TeamManagementComponent } from './pages/team-management/team-management.component';
import { AdminRegionsPageComponent } from './pages/admin-regions/admin-regions-page.component';
import { RoleLandingComponent } from './pages/role-landing/role-landing.component';
import { ForcePasswordChangeComponent } from './pages/force-password-change/force-password-change.component';
import { ProfileComponent } from './pages/profile/profile.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: 'login', component: LoginComponent, title: 'IIS Drug CRM | Login' },
	{
		path: 'force-password-change',
		component: ForcePasswordChangeComponent,
		canActivate: [authGuard],
		title: 'IIS Drug CRM | Force Password Change',
	},
	{
		path: 'admin/users',
		component: AdminUsersPageComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_ADMIN'] },
		title: 'IIS Drug CRM | Admin CRUD Panel',
	},
	{
		path: 'admin/regions',
		component: AdminRegionsPageComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_ADMIN'] },
		title: 'IIS Drug CRM | Regions',
	},
	{
		path: 'profile',
		component: ProfileComponent,
		canActivate: [authGuard, passwordChangeGuard],
		title: 'IIS Drug CRM | Profile',
	},
	{
		path: 'teams',
		component: TeamManagementComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PRICELIST_CREATOR'] },
		title: 'IIS Drug CRM | Teams',
	},
	{
		path: 'content',
		component: RoleLandingComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: ['ROLE_PRICELIST_CREATOR'],
			title: 'Content workspace',
			subtitle: 'Pricelist owner landing page.',
		},
		title: 'IIS Drug CRM | Content',
	},
	{
		path: 'published-pricelists',
		component: RoleLandingComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: ['ROLE_BUYER'],
			title: 'Published pricelists',
			subtitle: 'Buyer landing page.',
		},
		title: 'IIS Drug CRM | Pricelists',
	},
	{ path: '**', redirectTo: 'login' },
];

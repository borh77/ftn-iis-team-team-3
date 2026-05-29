import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { passwordChangeGuard } from './core/auth/password-change.guard';
import { CreateDoctorReportComponent } from './features/adverse-effects/pages/create-doctor-report/create-doctor-report.component';
import { MyReportsComponent } from './features/adverse-effects/pages/my-reports/my-reports.component';
import { AllReportsComponent } from './features/adverse-effects/pages/all-reports/all-reports.component';
import { ReportDetailComponent } from './features/adverse-effects/pages/report-detail/report-detail.component';
import { CreatePatientReportComponent } from './features/adverse-effects/pages/create-patient-report/create-patient-report.component';
import { EditReportComponent } from './features/adverse-effects/pages/edit-report/edit-report.component';
import { LoginComponent } from './pages/login/login.component';
import { AdminUsersPageComponent } from './pages/admin-users/admin-users-page.component';
import { TeamManagementComponent } from './pages/team-management/team-management.component';
import { AdminRegionsPageComponent } from './pages/admin-regions/admin-regions-page.component';
import { RoleLandingComponent } from './pages/role-landing/role-landing.component';
import { ForcePasswordChangeComponent } from './pages/force-password-change/force-password-change.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { SalesDashboardComponent } from './pages/sales-dashboard/sales-dashboard.component';
import { LeadsListComponent } from './features/sales/pages/leads-list/leads-list.component';
import { CustomersListComponent } from './features/sales/pages/customers-list/customers-list.component';
import { ProcessesListComponent } from './features/sales/pages/processes-list/processes-list.component';
import { CommunicationsListComponent } from './features/sales/pages/communications-list/communications-list.component';

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
	{
		path: 'sales',
		component: SalesDashboardComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: ['ROLE_SALES_REPRESENTATIVE', 'ROLE_ACCOUNT_MANAGER', 'ROLE_SALES_MANAGER'],
		},
		title: 'IIS Drug CRM | Sales',
	},
	{
		path: 'sales/leads',
		component: LeadsListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Leads',
	},
	{
		path: 'sales/customers',
		component: CustomersListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Customers',
	},
	{
		path: 'sales/processes',
		component: ProcessesListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Sales Pipeline',
	},
	{
		path: 'sales/communications',
		component: CommunicationsListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
		  roles: [
				'ROLE_SALES_REPRESENTATIVE',
				'ROLE_ACCOUNT_MANAGER',
				'ROLE_SALES_MANAGER',
		  ],
		},
		title: 'IIS Drug CRM | Communications',
	},
	// Podsistem neželjenih efekata lekova
	{
		path: 'adverse-effects/create-doctor-report',
		component: CreateDoctorReportComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_LEKAR'] },
		title: 'IIS Drug CRM | New Report — Doctor',
	},
	{
		path: 'adverse-effects/my-reports',
		component: MyReportsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_LEKAR'] },
		title: 'IIS Drug CRM | My Reports',
	},
	{
		path: 'adverse-effects/all-reports',
		component: AllReportsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_FARMAKOVIGILANT'] },
		title: 'IIS Drug CRM | All Reports',
	},
	{
		path: 'adverse-effects/create-patient-report',
		component: CreatePatientReportComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PACIJENT'] },
		title: 'IIS Drug CRM | Report Adverse Effect',
	},
	{
		path: 'adverse-effects/edit-report/:id',
		component: EditReportComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_LEKAR'] },
		title: 'IIS Drug CRM | Edit Report',
	},
	{
		path: 'adverse-effects/report/:id',
		component: ReportDetailComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_FARMAKOVIGILANT', 'ROLE_LEKAR'] },
		title: 'IIS Drug CRM | Report Details',
	},
	{ path: '**', redirectTo: 'login' },
];

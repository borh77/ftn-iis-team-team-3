import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { passwordChangeGuard } from './core/auth/password-change.guard';
import { CreateDoctorReportComponent } from './features/adverse-effects/pages/create-doctor-report/create-doctor-report.component';
import { MyReportsComponent } from './features/adverse-effects/pages/my-reports/my-reports.component';
import { AllReportsComponent } from './features/adverse-effects/pages/all-reports/all-reports.component';
import { AdverseAnalyticsComponent } from './features/adverse-effects/pages/analytics/adverse-analytics.component';
import { ReportDetailComponent } from './features/adverse-effects/pages/report-detail/report-detail.component';
import { CreatePatientReportComponent } from './features/adverse-effects/pages/create-patient-report/create-patient-report.component';
import { EditReportComponent } from './features/adverse-effects/pages/edit-report/edit-report.component';
import { LoginComponent } from './pages/login/login.component';
import { AdminUsersPageComponent } from './pages/admin-users/admin-users-page.component';
import { AdminLogsComponent } from './pages/admin-logs/admin-logs.component';
import { AdminReportsComponent } from './pages/admin-reports/admin-reports.component';
import { TeamManagementComponent } from './pages/team-management/team-management.component';
import { AdminRegionsPageComponent } from './pages/admin-regions/admin-regions-page.component';
import { RoleLandingComponent } from './pages/role-landing/role-landing.component';
import { ForcePasswordChangeComponent } from './pages/force-password-change/force-password-change.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { PortfolioPageComponent } from './pages/portfolio/portfolio-page.component';
import { PricelistCreateWizardComponent } from './pages/pricelist-create-wizard/pricelist-create-wizard.component';
import { SalesDashboardComponent } from './pages/sales-dashboard/sales-dashboard.component';
import { BuyerCatalogComponent } from './pages/buyer-catalog/buyer-catalog.component';
import { OrderUploadComponent } from './pages/order-upload/order-upload.component';
import { ProcurementListComponent } from './pages/procurement-list/procurement-list.component';
import { LeadsListComponent } from './features/sales/pages/leads-list/leads-list.component';
import { CustomersListComponent } from './features/sales/pages/customers-list/customers-list.component';
import { ProcessesListComponent } from './features/sales/pages/processes-list/processes-list.component';
import { CommunicationsListComponent } from './features/sales/pages/communications-list/communications-list.component';
import { OffersListComponent } from './features/sales/pages/offers-list/offers-list.component';
import { ContractsListComponent } from './features/sales/pages/contracts-list/contracts-list.component';
import { ProcessDetailsComponent } from './features/sales/pages/process-details/process-details.component';
import { CustomerDetailsComponent } from './features/sales/pages/customer-details/customer-details.component';
import { ContractDetailsComponent } from './features/sales/pages/contract-details/contract-details.component';
import { WorkflowEditorComponent } from './features/sales/pages/workflow-editor/workflow-editor.component';

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
		path: 'admin/activity-logs',
		component: AdminLogsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_ADMIN'] },
		title: 'IIS Drug CRM | Activity Logs',
	},
	{
		path: 'admin/reports',
		component: AdminReportsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_ADMIN'] },
		title: 'IIS Drug CRM | Performance Reports',
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
		path: 'content/mine',
		loadComponent: () => import('./pages/pricelist-list/pricelist-list.component').then(m => m.PricelistListComponent),
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PRICELIST_CREATOR'] },
		title: 'IIS Drug CRM | My Drafts',
	},
	{
		path: 'content/new',
		component: PricelistCreateWizardComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PRICELIST_CREATOR'] },
		title: 'IIS Drug CRM | New Pricelist',
	},
	{
		path: 'pricelists/create',
		component: PricelistCreateWizardComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PRICELIST_CREATOR'] },
		title: 'IIS Drug CRM | New Pricelist',
	},
	{
		path: 'pricelists/create/:id',
		component: PricelistCreateWizardComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PRICELIST_CREATOR'] },
		title: 'IIS Drug CRM | Continue Pricelist',
	},
	{
		path: 'pricelists/:id/edit',
		component: PricelistCreateWizardComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PRICELIST_CREATOR'] },
		title: 'IIS Drug CRM | Edit Pricelist Draft',
	},
	{
		path: 'published-pricelists',
		component: BuyerCatalogComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_BUYER'] },
		title: 'IIS Drug CRM | Pricelists',
	},
	{
		path: 'catalog',
		component: BuyerCatalogComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_BUYER'] },
		title: 'IIS Drug CRM | Medicine Catalog',
	},
	{
		path: 'buyer/order-validation',
		component: OrderUploadComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_BUYER'] },
		title: 'IIS Drug CRM | Order Validation',
	},
	{
		path: 'procurements',
		component: ProcurementListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_BUYER'] },
		title: 'IIS Drug CRM | My Procurements',
	},
	{
		path: 'portfolio',
		component: PortfolioPageComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_PORTFOLIO_MANAGER'] },
		title: 'IIS Drug CRM | Product Portfolio',
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
		path: 'sales/customers/:id',
		component: CustomerDetailsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Customer Details',
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
		path: 'sales/workflows',
		component: WorkflowEditorComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: ['ROLE_SALES_MANAGER'],
		},
		title: 'IIS Drug CRM | Sales Workflow Editor',
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
	// Adverse drug effects subsystem
	{
		path: 'adverse-effects/create-doctor-report',
		component: CreateDoctorReportComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_LEKAR'] },
		title: 'IIS Drug CRM | New Report - Doctor',
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
		path: 'adverse-effects/analytics',
		component: AdverseAnalyticsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: { roles: ['ROLE_FARMAKOVIGILANT'] },
		title: 'IIS Drug CRM | Adverse Effect Analytics',
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
	{
		path: 'sales/offers',
		component: OffersListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Offers',
	},
	{
		path: 'sales/contracts',
		component: ContractsListComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Contracts',
	},
	{
		path: 'sales/processes/:id',
		component: ProcessDetailsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Process Details',
	},
	{
		path: 'sales/contracts/:id',
		component: ContractDetailsComponent,
		canActivate: [authGuard, passwordChangeGuard],
		data: {
			roles: [
			'ROLE_SALES_REPRESENTATIVE',
			'ROLE_ACCOUNT_MANAGER',
			'ROLE_SALES_MANAGER',
			],
		},
		title: 'IIS Drug CRM | Contract Details',
	},
	{ path: '**', redirectTo: 'login' },
];

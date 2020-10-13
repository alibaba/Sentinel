import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DegradeComponent } from './degrade.component';

const routes: Routes = [
  { path: '', component: DegradeComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DegradeRoutingModule { }

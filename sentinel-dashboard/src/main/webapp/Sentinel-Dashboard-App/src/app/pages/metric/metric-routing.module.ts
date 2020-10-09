import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MetricComponent } from './metric.component';

const routes: Routes = [
  { path: '', component: MetricComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MetricRoutingModule { }

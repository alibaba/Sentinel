import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ParamFlowComponent} from "./param-flow.component";

const routes: Routes = [
  { path: '', component: ParamFlowComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ParamFlowRoutingModule { }

import { NgModule } from '@angular/core';
import { AppFilterPipe } from './pipe/app-filter.pipe';
import { FlowFilterPipe } from './pipe/flow-filter.pipe';
import { IdentityFilterPipe } from './pipe/identity-filter.pipe';

@NgModule({
  declarations: [
    AppFilterPipe, 
    FlowFilterPipe, 
    IdentityFilterPipe
  ],
  imports: [
  ],
  exports: [
    AppFilterPipe,
    FlowFilterPipe,
    IdentityFilterPipe
  ]
})
export class CommonModule { }
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {ParamFlowRoutingModule} from './param-flow-routing.module';

import {CreateParamFlowComponent} from './create-param-flow/create-param-flow.component';
import {DeleteParamFlowComponent} from './delete-param-flow/delete-param-flow.component';
import {UpdateParamFlowComponent} from './update-param-flow/update-param-flow.component';
import {ParamFlowComponent} from './param-flow.component';

import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzTableModule} from 'ng-zorro-antd/table';
import {NzDividerModule} from 'ng-zorro-antd/divider';
import {NzModalModule} from 'ng-zorro-antd/modal';
import {NzFormModule} from 'ng-zorro-antd/form';
import {NzInputModule} from 'ng-zorro-antd/input';
import {NzRadioModule} from 'ng-zorro-antd/radio';
import {NzSwitchModule} from 'ng-zorro-antd/switch';
import {NzGridModule} from 'ng-zorro-antd/grid';
import {NzSelectModule} from 'ng-zorro-antd/select';
import {NzAutocompleteModule} from 'ng-zorro-antd/auto-complete';
import {NzMessageModule} from 'ng-zorro-antd/message';

import {CommonModule as PrivateCommonModule} from 'src/app/common/common.module';

@NgModule({
  declarations: [
    ParamFlowComponent,
    CreateParamFlowComponent,
    DeleteParamFlowComponent,
    UpdateParamFlowComponent
  ],
  imports: [
    CommonModule,
    ParamFlowRoutingModule,
    NzIconModule,
    NzButtonModule,
    NzTableModule,
    NzDividerModule,
    NzModalModule,
    NzFormModule,
    FormsModule,
    ReactiveFormsModule,
    NzInputModule,
    NzRadioModule,
    NzSwitchModule,
    NzGridModule,
    NzSelectModule,
    NzAutocompleteModule,
    NzMessageModule,
    PrivateCommonModule
  ]
})
export class ParamFlowModule {
}

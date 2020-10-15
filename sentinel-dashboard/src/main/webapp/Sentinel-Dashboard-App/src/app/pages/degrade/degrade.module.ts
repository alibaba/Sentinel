import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { DegradeRoutingModule } from './degrade-routing.module';

import { DegradeComponent } from './degrade.component';
import { CreateDegradeComponent } from './create-degrade/create-degrade.component';

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzAutocompleteModule } from 'ng-zorro-antd/auto-complete';
import { NzMessageModule } from 'ng-zorro-antd/message';
import { UpdateDegradeComponent } from './update-degrade/update-degrade.component';
import { DeleteDegradeComponent } from './delete-degrade/delete-degrade.component';

import { CommonModule as PrivateCommonModule } from 'src/app/common/common.module';

@NgModule({
  declarations: [
    DegradeComponent,
    CreateDegradeComponent,
    UpdateDegradeComponent,
    DeleteDegradeComponent
  ],
  imports: [
    CommonModule,
    DegradeRoutingModule,
    NzButtonModule,
    NzIconModule,
    NzTableModule,
    NzDividerModule,
    NzFormModule,
    FormsModule,
    ReactiveFormsModule,
    NzModalModule,
    NzInputModule,
    NzRadioModule,
    NzGridModule,
    NzAutocompleteModule,
    NzMessageModule,
    PrivateCommonModule
  ],
  exports: [
    DegradeComponent
  ]
})
export class DegradeModule { }

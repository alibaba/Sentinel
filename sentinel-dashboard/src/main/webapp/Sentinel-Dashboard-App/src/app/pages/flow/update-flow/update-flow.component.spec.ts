import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateFlowComponent } from './update-flow.component';

describe('UpdateFlowComponent', () => {
  let component: UpdateFlowComponent;
  let fixture: ComponentFixture<UpdateFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

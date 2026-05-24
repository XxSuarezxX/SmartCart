import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Recomendados } from './recomendados';

describe('Recomendados', () => {
  let component: Recomendados;
  let fixture: ComponentFixture<Recomendados>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Recomendados],
    }).compileComponents();

    fixture = TestBed.createComponent(Recomendados);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

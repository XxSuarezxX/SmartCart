import { TestBed } from '@angular/core/testing';

import { Interaccion } from './interaccion';

describe('Interaccion', () => {
  let service: Interaccion;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Interaccion);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

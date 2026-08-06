import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-subscribe-payment',
  imports: [RouterLink, MatIconModule],
  templateUrl: './subscribe-payment.html',
  styleUrl: './subscribe-payment.scss',
})
export class SubscribePayment {}

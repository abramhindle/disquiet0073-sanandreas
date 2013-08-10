#!/usr/bin/env perl
# Copyright (C) 2013 Abram Hindle
#
# This program is free software, you can redistribute it and/or modify it
# under the terms of the Artistic License version 2.0.
#
# This program basically relays web based OSC events to your OSC host!
#
# Send an XMLHTTPRequest full of json of the form:
# { "queue":[
#       ["osccommand1","i",100,"f",100.0,"s","what"],
#       ["osccommand1","i",100,"f",100.0,"s","what"],
#       ["osccommand1","i",100,"f",100.0,"s","what"],
#       ["osccommand1","i",100,"f",100.0,"s","what"]
#   ]
# }
# And it will be sent out as an OSC Bundle to your OSC host
#
# To start this webservice just run:
#   hypnotoad -f oscrelay.pl
# or
#   perl oscrelay.pl daemon

use Mojolicious::Lite;
use strict;
use JSON;
use Data::Dumper;
use Net::OpenSoundControl::Client;
my %allowed = map { $_ => $_ } qw( colour );
my @paths = qw(/sanandreas /osc /sanandraes);
my $oschost = "127.0.0.1";
my $oscport = 57120;
{
  my $client = undef;
  sub client {
    if (!$client) {
      $client = Net::OpenSoundControl::Client->new(Host => $oschost, Port => $oscport );
    }
    return $client;
  }
}

sub oscResponder {
  my $self = shift;
  warn "OSC Responder!";
  my $body = $self->req->body();
  my @elms = ();
  my $code = {};
  eval {
    $code = from_json( $body );
  };
  if ($@) {
    warn $@;
    $code = {};
  }
  if ($code->{queue}) {
    warn Dumper($code);
    foreach my $elm (@{$code->{queue}}) {
      my ($type,@args) = @$elm;
      if ($allowed{$type}) {
        my $command = $allowed{$type};
        push @elms,['/'.$command, @args];
      } else {
        my $err = "Not allowed: [$type]".$allowed{$type};
        warn $err;
        $self->respond_to( any => { data => $err }, status => 200 );
      }
    }
  }
  print Dumper(\@elms);
  client()->send(['#bundle', 0,@elms]) if @elms;
  my $state = {"ok"=>"ok"};
  my $str = encode_json($state);
  # do we need some raw crap
  $self->respond_to(any => {data=>$str}, status => 200);
}

foreach my $path (@paths) {
  get $path => sub { oscResponder(@_) };
  post $path => sub { oscResponder(@_) };
}

app->start;

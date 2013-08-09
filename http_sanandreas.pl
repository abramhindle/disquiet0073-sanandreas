#!/usr/bin/perl
use strict;
use Mongrel2;
use JSON;
use IO::Socket;
use Net::OpenSoundControl::Client;
use Data::Dumper;
my %allowed = qw( colour colour);

{
	my $client = undef;

	sub client {
		return $client if ($client);
		$client = Net::OpenSoundControl::Client->new(Host => "127.0.0.1", Port => 57120);
		return $client;
	}
}

my $sender_id = "d4b49f5a-a1a6-4c7b-b851-3987b296c5a2";

my $sub_addr = "tcp://127.0.0.1:9947";
my $pub_addr = "tcp://127.0.0.1:9946";

my $mongrel = Mongrel2::mongrel_init( $sender_id, $sub_addr, $pub_addr );

my $cnt=0;
while(1) {
    print "WAITING FOR REQUEST$/";
    my $req = $mongrel->recv( );
    if ($mongrel->is_disconnect( $req )) {
        print "DISCONNECT";
        next;
    } else {
        print $cnt++.$/;#.$response.$/;
        #print Dumper($req);
        if ($req->{body}) {
            warn "Got Body";
            eval {
                my @elms = ();
                my $code = from_json( $req->{body} );
                if ($code->{queue}) {
                    warn Dumper($code);
                    foreach my $elm (@{$code->{queue}}) {
                        my ($type,@args) = @$elm;
                        if ($allowed{$type}) {
                            my $command = $allowed{$type};
                            push @elms,['/'.$command, @args];
                        } else {
                            warn "Not allowed: [$type]".$allowed{$type};
                        }
                    }
                }
                print Dumper(\@elms);
                client()->send(['#bundle', 0,@elms]) if @elms;
            };
            if ($@) {
                $mongrel->reply_http( $req, "COULD NOT SEND $cnt : $@");
            } else {
                # now get state and return it
                # step 1. naively keep returning the state back to the user
                #my $state = Enveloper::get_state();
                my $state = {"ok"=>"ok"};
                my $str = encode_json($state);
                $mongrel->reply_http( $req, $str);#, 200, "OK", {application/jsonapplication);
            }
        } else {
            $mongrel->reply_http( $req, "NO BODY [bubble] $cnt");
        }
    }
}


# Gee instead of a prefix maybe you should make a module?


kernel void Grav(global const float *X, global const float *Y, global const float *Z,
					global float *Xa, global float *Ya, global float *Za, global const float *M)
{
	
	int gid = get_global_id(0);
	int n = get_global_size(0);
	
	if (gid < n) {
		float3 Acc = {0.0f,0.0f,0.0f};
		float3 Pos = {X[gid], Y[gid], Z[gid]};
		
		float3 dist;
		float distMag;
		
		for (int i = 0; i < n; i++)
		{		
			dist = (float3){X[i], Y[i], Z[i]} - Pos;
		
			distMag = (sqrt((dist.x*dist.x)  + (dist.y*dist.y) + (dist.z*dist.z))) + 0.0000001f;
			
			//Gravity
			Acc +=  dist * (M[i]/ ((distMag*distMag)));
		}
		
		Xa[gid] = Acc.x;
		Ya[gid] = Acc.y;
		Za[gid] = Acc.z;
	}
}

kernel void Step(global float *X, global float *Y, global float *Z,
					global float *Xv, global float *Yv, global float *Zv,
					global float *Xa, global float *Ya, global float *Za,
					global float *M) 
{
	int gid = get_global_id(0);
	
	if (gid < get_global_size(0)) {
		Xv[gid] += Xa[gid];
		Yv[gid] += Ya[gid];
		Zv[gid] += Za[gid];
	
		X[gid] += Xv[gid];
		Y[gid] += Yv[gid];
		Z[gid] += Zv[gid];
	}
}
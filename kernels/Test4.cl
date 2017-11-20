kernel void Grav(global const float *X, global const float *Y, global const float *M,
					global float *Xa, global float *Ya)
{
	int gid = get_global_id(0);
	int n = get_global_size(0);
	
	if (gid < n) {
	
		float2 Acc = {0.0f,0.0f};
		float2 Pos = {X[gid], Y[gid]};
		float2 dist;
		float distMag;
		
		for(int i = 0; i < n; i++) {
			dist = (float2){X[i],Y[i]} - Pos;
		
			distMag = rsqrt((dist.x*dist.x)  + (dist.y*dist.y) + 0.0001f);
			
			//Gravity
			Acc +=  dist * M[i] * distMag*distMag;
		}
		
		
		Xa[gid] = Acc.x;
		Ya[gid] = Acc.y;
	}
}

kernel void GravShared(global const float *X, global const float *Y, global const float *M,
					global float *Xa, global float *Ya,
					local float4 *pblock)
{
	int gid = get_global_id(0);
	int lid = get_local_id(0);
	
	int n = get_global_size(0);
	int nt = get_local_size(0);
	int nb = n/nt;
	
	if (gid < n) {
	
		float4 Acc = {0.0f,0.0f,0.0f, 0.0f};
		float4 Pos = {X[gid], Y[gid], 0.0f, 0.0f};
		float4 dist;
		float distMag;
		
		for(int jb = 0; jb < nb; jb++) {
			
			pblock[lid].x = X[jb*nt+lid];
			pblock[lid].y = Y[jb*nt+lid];
			pblock[lid].z = 0.0f;
			pblock[lid].w = M[jb*nt+lid];
			
			barrier(CLK_LOCAL_MEM_FENCE);
			
			for(int j = 0; j < nt; j++) {
				dist = pblock[j] - Pos;
			
				distMag = rsqrt((dist.x*dist.x)  + (dist.y*dist.y) + 0.0001f);
				
				//Gravity
				Acc +=  dist * dist.w * distMag*distMag;
			}
		}
		
		Xa[gid] = Acc.x;
		Ya[gid] = Acc.y;
	}
}

kernel void StepSingle(global float *X, global float *Y,
					global float *Xv, global float *Yv,
					global float *Xa, global float *Ya,
					global float *M) 
{
	int gid = get_global_id(0);
	
	if (gid < get_global_size(0)) {
		Xv[gid] += Xa[gid];
		Yv[gid] += Ya[gid];
	
		X[gid] += Xv[gid];
		Y[gid] += Yv[gid];
	}
}

kernel void GravMulti(global const float *X, global const float *Y, global const float *M,
					global float *Xa, global float *Ya,
					int size, int offset)
{
	int gid = get_global_id(0);
	int n = get_global_size(0);
	
	if (gid < n) {
		float2 Acc = {0.0f,0.0f};
		float2 Pos = {X[gid+offset], Y[gid+offset]};
		
		float2 dist;
		float distMag;
		
		for (int i = 0; i < size; i++)
		{		
			dist = (float2){X[i], Y[i]} - Pos;
		
			distMag = (sqrt((dist.x*dist.x)  + (dist.y*dist.y))) + 0.0001f;
			
			//Gravity
			Acc +=  dist * (M[i]/ ((distMag*distMag)));
		}
		
		Xa[gid] = Acc.x;
		Ya[gid] = Acc.y;
	}
}


